package org.infinispan.chaos.environment;

import org.infinispan.chaos.proxy.OpenShiftProxy;
import org.infinispan.chaos.proxy.Proxy;

public class OpenShiftEnvironment implements Environment {

   @Override
   public String cmd() {
      return "oc";
   }

   @Override
   public Proxy createProxy(String namespace, String podName) {
      return new OpenShiftProxy(namespace, podName);
   }
}
