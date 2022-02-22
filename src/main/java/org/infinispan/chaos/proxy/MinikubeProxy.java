package org.infinispan.chaos.proxy;

import java.io.IOException;

import org.infinispan.chaos.io.ProcessWrapper;
import org.infinispan.chaos.net.TcpPort;

public class MinikubeProxy implements Proxy {

   private String namespace;
   private String podName;
   private ProcessWrapper process;
   private int freePort;

   public MinikubeProxy(String namespace, String podName) {
      this.namespace = namespace;
      this.podName = podName;
      this.process = new ProcessWrapper();
   }

   public void start() {
      try {
         this.freePort = TcpPort.nextFreePort();
         process.start(String.format("kubectl port-forward -n %s %s %d:%d", namespace, podName, freePort, 11222));
         // Forwarding from 127.0.0.1:8081 -> 11222
         // Forwarding from [::1]:8081 -> 11222
         process.read();
         process.read();
      } catch (IOException e) {
         throw new IllegalStateException(e);
      }
   }

   public void stop() {
      process.destroy();
   }

   @Override
   public int getClientPort() {
      return this.freePort;
   }
}
