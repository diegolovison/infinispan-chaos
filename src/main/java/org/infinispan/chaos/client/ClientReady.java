package org.infinispan.chaos.client;

import java.util.Map;

import org.infinispan.chaos.hotrod.HotRodClient;

@FunctionalInterface
public interface ClientReady {

   void run(Map<String, HotRodClient> hotRodConnectors);
}
