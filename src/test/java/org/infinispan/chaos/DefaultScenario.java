package org.infinispan.chaos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.chaos.client.ClientReady;
import org.infinispan.chaos.hotrod.HotRodClient;
import org.infinispan.chaos.hotrod.HotRodClientPool;
import org.infinispan.chaos.io.Sleep;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.marshall.MarshallerUtil;
import org.infinispan.commons.marshall.AdaptiveBufferSizePredictor;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.util.Util;

public abstract class DefaultScenario implements ClientReady {

   private static final Logger log = LogManager.getLogger(DefaultScenario.class);

   private String name;
   private final ChaosTesting chaosTesting;
   private AdaptiveBufferSizePredictor sizePredictor;
   private final double pct = Double.valueOf(System.getProperty("infinispan-chaos.it_pct", "1"));

   public DefaultScenario(String name, ChaosTesting chaosTesting) {
      this.name = name;
      this.chaosTesting = chaosTesting;
      this.sizePredictor = new AdaptiveBufferSizePredictor();
   }

   @Override
   public void run(HotRodClientPool pool) {
      try {
         int maxValues = (int) (10000 * pct);
         for (int i = 0; i < maxValues; i++) {
            String key = this.name + "-" + i;
            long begin = System.currentTimeMillis();
            String byteKey = null;
            while (true) {
               HotRodClient client = pool.next();
               try {
                  RemoteCache remoteCache = client.getRemoteCache();
                  if (byteKey == null) {
                     byteKey = keyToByte(remoteCache, key);
                  }
                  log.debug(String.format("Using client %s to put key %s", client, byteKey));
                  remoteCache.put(key, "value-" + i);
                  log.debug(String.format("After put: %s elapsed %d", byteKey, ((System.currentTimeMillis() - begin) / 1000)));
                  if (i == maxValues / 2) {
                     log.info("Introducing failure");
                     introduceFailure();
                     log.info("Failure Introduced");
                  }
                  break;
               } catch (Exception e) {
                  log.error(String.format("Error while executing a put: %s Message: %s", byteKey, e.getMessage()));
                  Sleep.sleep(1000);
               }
            }
         }
         RemoteCache remoteCache = pool.next().getRemoteCache();
         for (int i = 0; i < maxValues; i++) {
            assert remoteCache.get("key-" + i) != null;
         }
      } finally {
         chaosTesting.stop();
      }
   }

   private String keyToByte(RemoteCache remoteCache, String key) {
      Marshaller keyMarshaller = remoteCache.getRemoteCacheManager().getMarshaller();
      byte[] byteKey = MarshallerUtil.obj2bytes(keyMarshaller, key, sizePredictor);
      return Util.printArray(byteKey);
   }

   public abstract void introduceFailure();
}
