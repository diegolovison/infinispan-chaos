package org.infinispan.chaos;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.chaos.client.ClientReady;
import org.infinispan.chaos.hotrod.HotRodClient;
import org.infinispan.chaos.io.Sleep;
import org.infinispan.client.hotrod.RemoteCache;

public abstract class DefaultScenario implements ClientReady {

   private static final Logger log = LogManager.getLogger(DefaultScenario.class);

   private String name;
   private final ChaosTesting chaosTesting;
   private final double pct = Double.valueOf(System.getProperty("infinispan-chaos.it_pct", "1"));

   public DefaultScenario(String name, ChaosTesting chaosTesting) {
      this.name = name;
      this.chaosTesting = chaosTesting;
   }

   @Override
   public void run(Map<String, HotRodClient> clients) {
      try {
         int maxValues = (int) (10000 * pct);
         for (int i = 0; i < maxValues; i++) {
            String key = this.name + "-" + i;
            long begin = System.currentTimeMillis();
            log.info(String.format("Before put: %s", key));
            while (true) {
               try {
                  HotRodClient client = clients.values().iterator().next();
                  RemoteCache remoteCache = client.getRemoteCache();
                  remoteCache.put(key, "value-" + i);
                  log.info(String.format("After put: %s elapsed %d", key, ((System.currentTimeMillis() - begin) / 1000)));
                  if (i == maxValues / 2) {
                     log.info("Introducing failure");
                     introduceFailure();
                     log.info("Failure Introduced");
                  }
                  break;
               } catch (Exception e) {
                  log.error(String.format("Error while executing a put: %s Message: %s", key, e.getMessage()));
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
