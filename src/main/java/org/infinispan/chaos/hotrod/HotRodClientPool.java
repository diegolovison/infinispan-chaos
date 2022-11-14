package org.infinispan.chaos.hotrod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HotRodClientPool {

   private static final Logger log = LogManager.getLogger(HotRodClientPool.class);

   private final List<String> keys;
   private final Map<String, HotRodClient> clients;

   public HotRodClientPool() {
      this.keys = new ArrayList<>();
      this.clients = new HashMap<>();
   }

   public HotRodClient next() {
      int idx = (int)(Math.random() * size());
      HotRodClient client = get(keys.get(idx));
      return client;
   }

   public boolean containsKey(String podName) {
      return this.clients.containsKey(podName);
   }

   public HotRodClient get(String podName) {
      return this.clients.get(podName);
   }

   public void remove(String podName) {
      HotRodClient hotRodClient = this.clients.get(podName);
      if (hotRodClient != null) {
         log.info(String.format("%s removed from the pool", hotRodClient));
         hotRodClient.close();
         this.clients.remove(podName);
      }
      this.keys.remove(podName);
      this.clients.remove(podName);
   }

   public void put(String podName, HotRodClient hotRodClient) {
      this.keys.add(podName);
      this.clients.put(podName, hotRodClient);
   }

   public int size() {
      return this.clients.size();
   }

   public void close() {
      this.clients.values().forEach((client) -> client.close());
   }

   public Collection<HotRodClient> getClients() {
      return clients.values();
   }
}
