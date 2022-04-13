package org.infinispan.chaos.proxy;

import org.infinispan.chaos.environment.Environment;

public class OpenShiftProxy extends DefaultProxy implements Proxy {

   public OpenShiftProxy(String namespace, String podName, Environment environment) {
      super(namespace, podName, environment);
   }
}
