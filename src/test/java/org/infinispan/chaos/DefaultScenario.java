package org.infinispan.chaos;

import java.util.Map;

import org.infinispan.chaos.client.ClientReady;
import org.infinispan.chaos.hotrod.HotRodClient;
import org.infinispan.chaos.io.Sleep;
import org.infinispan.client.hotrod.RemoteCache;

public abstract class DefaultScenario implements ClientReady {

   private final ChaosTesting chaosTesting;

   public DefaultScenario(ChaosTesting chaosTesting) {
      this.chaosTesting = chaosTesting;
   }

   @Override
   public void run(Map<String, HotRodClient> clients) {
      try {
         int maxValues = 10000;
         for (int i = 0; i < maxValues; i++) {
            String key = "key-" + i;
            while (true) {
               try {
                  HotRodClient client = clients.values().iterator().next();
                  RemoteCache remoteCache = client.getRemoteCache();
                  remoteCache.put(key, "value-" + i);
                  if (i == 5000) {
                     introduceFailure();
                  }
                  break;
               } catch (Exception e) {
                  System.err.println("Error while executing a put: '" + key + "' Message: " + e.getMessage());
                  Sleep.sleep(1000);
               }
            }
         }
         RemoteCache remoteCache = clients.values().iterator().next().getRemoteCache();
         for (int i = 0; i < maxValues; i++) {
            assert remoteCache.get("key-" + i) != null;
         }
      } finally {
         chaosTesting.stop();
      }
   }

   public abstract void introduceFailure();
}
