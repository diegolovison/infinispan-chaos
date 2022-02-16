package org.infinispan.chaos.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

public class TcpPort {

   public static int nextFreePort() {
      int from = 8080;
      int to = 9999;
      while (true) {
         int port = ThreadLocalRandom.current().nextInt(from, to);
         if (isLocalPortFree(port)) {
            return port;
         }
      }
   }

   private static boolean isLocalPortFree(int port) {
      try {
         new ServerSocket(port).close();
         return true;
      } catch (IOException e) {
         return false;
      }
   }
}
