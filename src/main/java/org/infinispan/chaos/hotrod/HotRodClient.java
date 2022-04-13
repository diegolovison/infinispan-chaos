package org.infinispan.chaos.hotrod;

import org.infinispan.chaos.exception.ChaosTestingException;
import org.infinispan.chaos.io.Sleep;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import org.infinispan.chaos.proxy.Proxy;

public class HotRodClient {

   private final String cacheName;
   private final Proxy proxy;
   private final RemoteCache remoteCache;

   public HotRodClient(String cacheName, Proxy proxy, org.infinispan.configuration.cache.ConfigurationBuilder cacheConfig) {
      this.cacheName = cacheName;
      this.proxy = proxy;
      this.remoteCache = createClient(cacheConfig);
   }

   public RemoteCache getRemoteCache() {
      return remoteCache;
   }

   public void close() {
      remoteCache.getRemoteCacheManager().close();
      proxy.stop();
   }

   private RemoteCache createClient(org.infinispan.configuration.cache.ConfigurationBuilder cacheConfig) {
      int retry = 10;
      int count = 0;
      int port = -1;
      while (count < retry) {
         try {
            proxy.start();

            port = proxy.getClientPort();

            // kubectl get secret example-infinispan-generated-secret -o jsonpath="{.data.identities\.yaml}" | base64 --decode
            ConfigurationBuilder builder = new ConfigurationBuilder();
            String username = System.getProperty("infinispan.client.hotrod.auth_username");
            if (username != null) {
               builder.security().authentication().username(username).password(System.getProperty("infinispan.client.hotrod.auth_password"));
            }
            builder.clientIntelligence(ClientIntelligence.BASIC);
            builder.addServer()
                  .host("127.0.0.1")
                  .port(port);

            RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
            for (String currentCacheName : cacheManager.getCacheNames()) {
               if (!cacheName.equals(currentCacheName) && !currentCacheName.startsWith("___") && !"default".equals(currentCacheName)) {
                  cacheManager.administration().removeCache(currentCacheName);
               }
            }
            cacheManager.administration().getOrCreateCache(cacheName, cacheConfig.build());
            return cacheManager.getCache(cacheName);
         } catch (Exception e) {
            proxy.stop();
            System.out.println(String.format("Cannot create cacheManager=%d: %s", port, e.getMessage()));
            e.printStackTrace();
            Sleep.sleep(10000);
            count++;
         }
      }
      throw new ChaosTestingException("Cannot create HotRod client");
   }
}
