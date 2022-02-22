package org.infinispan.chaos.proxy;

public interface Proxy {

   void start();

   void stop();

   default int getClientPort() {
      return 11222;
   }
}
