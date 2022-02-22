package org.infinispan.chaos.environment;

import org.infinispan.chaos.proxy.Proxy;

public interface Environment {

   String cmd();

   Proxy createProxy(String namespace, String podName);
}
