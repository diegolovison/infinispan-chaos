package org.infinispan.chaos.client;

import org.infinispan.chaos.hotrod.HotRodClientPool;

@FunctionalInterface
public interface ClientReady {

   void run(HotRodClientPool poll);
}
