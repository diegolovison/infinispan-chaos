package org.infinispan.chaos;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.persistence.sifs.configuration.SoftIndexFileStoreConfigurationBuilder;
import org.junit.jupiter.api.Test;

public class KillPodTest {

   @Test
   public void killPodTest() throws IOException {
      // cache config
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.persistence().addStore(SoftIndexFileStoreConfigurationBuilder.class);
      builder.clustering().stateTransfer().timeout(5, TimeUnit.MINUTES);
      builder.clustering().cacheMode(CacheMode.DIST_SYNC).hash().numOwners(2);
      builder.memory().maxCount(100);
      String cacheName = "cache_" + UUID.randomUUID();

      ChaosTesting chaosTesting = new ChaosTesting()
            .deploy("default_deploy.yaml")
            .expectedNumClients(3);

      DefaultScenario defaultScenario = new DefaultScenario(chaosTesting) {
         @Override
         public void introduceFailure() {
            chaosTesting.apply("kill-pod.yaml");
         }
      };
      chaosTesting.run(cacheName, builder, defaultScenario);
   }
}
