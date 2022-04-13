package org.infinispan.chaos.proxy;

import org.infinispan.chaos.environment.Environment;

public class MinikubeProxy extends DefaultProxy implements Proxy {

   public MinikubeProxy(String namespace, String podName, Environment environment) {
      super(namespace, podName, environment);
   }
}
