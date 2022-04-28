package org.infinispan.chaos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.chaos.client.ClientReady;
import org.infinispan.chaos.environment.Environment;
import org.infinispan.chaos.environment.MinikubeEnvironment;
import org.infinispan.chaos.environment.OpenShiftEnvironment;
import org.infinispan.chaos.exception.ChaosTestingException;
import org.infinispan.chaos.hotrod.HotRodClient;
import org.infinispan.chaos.hotrod.HotRodClientPool;
import org.infinispan.chaos.io.ProcessWrapper;
import org.infinispan.chaos.proxy.Proxy;
import org.infinispan.configuration.cache.ConfigurationBuilder;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import okhttp3.Call;
import okhttp3.OkHttpClient;

public class ChaosTesting {

   private static final Logger log = LogManager.getLogger(ChaosTesting.class);

   private final CoreV1Api api;
   private final HotRodClientPool hotRodPool;

   private String namespace;
   private Environment environment;
   private int expectedNumClients;
   private Watch<V1Pod> watch;
   private boolean started = false;
   private final List<String> applies;
   private final ScheduledExecutorService executor;

   private boolean watchClosed = false;

   public ChaosTesting() throws IOException {
      ApiClient client = Config.defaultClient();
      OkHttpClient httpClient = client.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
      client.setHttpClient(httpClient);
      Configuration.setDefaultApiClient(client);
      this.api = new CoreV1Api();
      this.hotRodPool = new HotRodClientPool();
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
         String command = String.format("%s apply -f %s -n %s", environment.cmd(), file, this.namespace);
         log.info(String.format("Executing: %s", command));
         process.start(command);
         // x created
         process.read();
         applies.add(file);
      } catch (IOException e) {
         throw new IllegalStateException(e);
      }
      ChaosTestingFailure failure = new ChaosTestingFailure(executor, file, environment, namespace);
      return failure;
   }

   public ChaosTesting run(String cacheName, ConfigurationBuilder cacheConfig, ClientReady clientReady) {
      this.executor.submit(() -> {
         try {
            Call call = api.listNamespacedPodCall(this.namespace, null, null, null, null, null, null, null, null, null, true, null);
            watch = Watch.createWatch(Config.defaultClient(), call, new TypeToken<Watch.Response<V1Pod>>() {}.getType());
            for (Watch.Response<V1Pod> item : watch) {
               V1ObjectMeta metadata = item.object.getMetadata();
               if (metadata.getLabels() == null || metadata.getLabels().size() == 0) {
                  log.debug(String.format("%s: meta data is empty", item));
                  continue;
               }
               String appLabel = metadata.getLabels().get("app");
               if (!"infinispan-pod".equals(appLabel)) {
                  log.debug(String.format("%s: has no app label equals infinispan-pod. Labels: %s", item, metadata.getLabels()));
                  continue;
               }
               String podName = metadata.getName();

               log.info(String.format("%s: %s%n", item.type, podName));

               if ("DELETED".equals(item.type)) {
                  this.hotRodPool.remove(podName);
               } else if ("Running".equals(item.object.getStatus().getPhase()) && !this.hotRodPool.containsKey(podName)) {
                  List<V1PodCondition> conditions = item.object.getStatus().getConditions();
                  boolean startHotRod = false;
                  for (V1PodCondition condition : conditions) {
                     if ("Ready".equals(condition.getType()) && "True".equals(condition.getStatus())) {
                        startHotRod = true;
                        break;
                     }
                  }
                  if (startHotRod) {
                     Proxy proxy = createProxy(namespace, podName);
                     HotRodClient hotRodClient = new HotRodClient(cacheName, proxy, cacheConfig);
                     log.info(String.format("%s added to the pool", hotRodClient));
                     this.hotRodPool.put(podName, hotRodClient);
                  }
                  if (!started && this.hotRodPool.size() == expectedNumClients) {
                     this.executor.submit(() -> clientReady.run(hotRodPool));
                     started = true;
                  }
               }
            }
         } catch (Exception e) {
            if (e instanceof ChaosTestingException) {
               throw (ChaosTestingException) e;
            } else {
               log.error("Something wrong: ", e);
            }
         }
      });
      try {
         this.executor.awaitTermination(1, TimeUnit.DAYS);
      } catch (InterruptedException e) {
         log.error(e);
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
            process.start(String.format("%s delete -f %s -n %s", this.environment.cmd(), file, this.namespace));
            // x delete
            process.read();
            process.destroy();
         } catch (Exception e) {
            // silent
         }
      }
      this.hotRodPool.close();
      closeWatch();
      this.executor.shutdownNow();
   }

   private void closeWatch() {
      try {
         log.info("Closing Watch");
         watch.close();
         watchClosed = true;
      } catch (Exception e) {
         // suppress
      }
   }

   public ChaosTesting deploy(String resourceFile) {
      apply(resourceFile);
      return this;
   }
}
