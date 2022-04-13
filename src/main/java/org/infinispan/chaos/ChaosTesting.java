package org.infinispan.chaos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.infinispan.chaos.call.LogCall;
import org.infinispan.chaos.client.ClientReady;
import org.infinispan.chaos.environment.Environment;
import org.infinispan.chaos.environment.MinikubeEnvironment;
import org.infinispan.chaos.environment.OpenShiftEnvironment;
import org.infinispan.chaos.hotrod.HotRodClient;
import org.infinispan.chaos.io.ProcessWrapper;
import org.infinispan.chaos.io.Sleep;
import org.infinispan.chaos.proxy.Proxy;
import org.infinispan.configuration.cache.ConfigurationBuilder;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import okhttp3.Call;
import okhttp3.OkHttpClient;

public class ChaosTesting {

   private final CoreV1Api api;
   private final Map<String, HotRodClient> hotRodConnectors;

   private String namespace;
   private Environment environment;
   private int expectedNumClients;
   private Watch<V1Pod> watch;
   private boolean started = false;
   private final List<String> applies;
   private final ScheduledExecutorService executor;

   public ChaosTesting() throws IOException {
      ApiClient client = Config.defaultClient();
      OkHttpClient httpClient = client.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
      client.setHttpClient(httpClient);
      Configuration.setDefaultApiClient(client);
      this.api = new CoreV1Api();
      this.hotRodConnectors = new HashMap<>();
      this.applies = new ArrayList<>();
      this.executor = Executors.newScheduledThreadPool(3);
      environment(System.getenv("CHAOS_TESTING_ENVIRONMENT"));
      namespace(System.getenv("CHAOS_TESTING_NAMESPACE"));
   }

   public ChaosTesting namespace(String namespace) {
      if (namespace == null || namespace.trim().length() == 0) {
         this.namespace = "ispn-testing";
      } else {
         this.namespace = namespace;
      }
      return this;
   }

   private ChaosTesting environment(String environment) {
      if (environment == null || environment.trim().length() == 0 || "minikube".equals(environment)) {
         this.environment = new MinikubeEnvironment();
      } else if ("openshift".equals(environment)) {
         this.environment = new OpenShiftEnvironment();
      } else {
         throw new IllegalStateException("Cannot create the proxy. Invalid argument: " + environment);
      }
      return this;
   }

   public ChaosTesting expectedNumClients(int expectedNumClients) {
      this.expectedNumClients = expectedNumClients;
      return this;
   }

   private String transformYaml(String file) {
      Path filePath = Path.of(file);
      String content;
      try {
         content = Files.readString(filePath);
      } catch (IOException e) {
         throw new IllegalStateException("Cannot transform yaml", e);
      }
      content = content.replaceAll("\\{ \\{ namespace \\} \\}", this.namespace);
      File tempFile;
      try {
         tempFile = File.createTempFile(filePath.getFileName().toString(), ".tmp");
      } catch (IOException e) {
         throw new IllegalStateException("Cannot create temp file", e);
      }
      try (FileWriter writer = new FileWriter(tempFile)) {
         writer.write(content);
      } catch (IOException e) {
         throw new IllegalStateException("Cannot create temp file", e);
      }
      return tempFile.getAbsolutePath();
   }

   public ChaosTestingFailure apply(String file) {
      URL url = ChaosTesting.class.getClassLoader().getResource(file);
      if (url == null) {
         throw new NullPointerException();
      }
      file = transformYaml(url.getFile());
      ProcessWrapper process = new ProcessWrapper();
      try {
         process.start(String.format("%s apply -f %s", environment.cmd(), file));
         // x created
         process.read();
         applies.add(file);
      } catch (IOException e) {
         throw new IllegalStateException(e);
      }
      ChaosTestingFailure failure = new ChaosTestingFailure(executor, file, environment);
      return failure;
   }

   public ChaosTesting run(String cacheName, ConfigurationBuilder cacheConfig, ClientReady clientReady) {
      try {
         Call call = api.listNamespacedPodCall(this.namespace, null, null, null, null, null, null, null, null, null, true, null);
         watch = Watch.createWatch(Config.defaultClient(), call, new TypeToken<Watch.Response<V1Pod>>() {}.getType());
         for (Watch.Response<V1Pod> item : watch) {
            V1ObjectMeta metadata = item.object.getMetadata();
            if (metadata.getLabels() == null || metadata.getLabels().size() == 0) {
               continue;
            }
            String appLabel = metadata.getLabels().get("app");
            if (!"infinispan-pod".equals(appLabel)) {
               continue;
            }
            String podName = metadata.getName();
            System.out.printf("%s : %s%n", item.type, podName);
            while (true) {
               boolean found = false;
               String log = LogCall.call(this.api, this.namespace, podName);
               // stopping
               if (log == null || log.contains("ISPN080002") && this.hotRodConnectors.containsKey(podName)) {
                  HotRodClient hotRodClient = this.hotRodConnectors.get(podName);
                  if (hotRodClient != null) {
                     hotRodClient.close();
                     this.hotRodConnectors.remove(podName);
                  }
                  found = true;
               // started
               } else if (log.contains("ISPN080001")) {
                  if (!this.hotRodConnectors.containsKey(podName)) {
                     Proxy proxy = createProxy(namespace, podName);
                     HotRodClient hotRodClient = new HotRodClient(cacheName, proxy, cacheConfig);
                     this.hotRodConnectors.put(podName, hotRodClient);
                  }
                  found = true;
               }
               if (found) {
                  System.out.println(this.hotRodConnectors);
                  if (!started && this.hotRodConnectors.size() == expectedNumClients) {
                     this.executor.submit(() -> clientReady.run(this.hotRodConnectors));
                     started = true;
                  }
                  break;
               } else {
                  Sleep.sleep(1000);
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return this;
   }

   private Proxy createProxy(String namespace, String podName) {
      return this.environment.createProxy(namespace, podName);
   }

   public void stop() {
      for (String file : applies) {
         ProcessWrapper process = new ProcessWrapper();
         try {
            process.start(String.format("%s delete -f %s", this.environment.cmd(), file));
            // x delete
            process.read();
            process.destroy();
         } catch (Exception e) {
            // silent
         }
      }
      this.hotRodConnectors.values().forEach((client) -> client.close());
      closeWatch();
      this.executor.shutdownNow();
   }

   private void closeWatch() {
      try {
         watch.close();
      } catch (Exception e) {
         // suppress
      }
   }

   public ChaosTesting deploy(String resourceFile) {
      apply(resourceFile);
      return this;
   }
}
