package org.infinispan.chaos.environment;

import org.infinispan.chaos.proxy.MinikubeProxy;
import org.infinispan.chaos.proxy.Proxy;

public class MinikubeEnvironment implements Environment {

   @Override
   public String cmd() {
      return "kubectl";
   }

   @Override
   public Proxy createProxy(String namespace, String podName) {
      return new MinikubeProxy(namespace, podName);
   }
}
