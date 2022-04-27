package org.infinispan.chaos.environment;

import org.infinispan.chaos.io.ProcessWrapper;
import org.infinispan.chaos.proxy.OpenShiftProxy;
import org.infinispan.chaos.proxy.Proxy;

public class OpenShiftEnvironment implements Environment {

   private final String url;
   private final String token;

   public OpenShiftEnvironment() {
      this.url = validateEnv("OPENSHIFT_URL");
      this.token = validateEnv("OPENSHIFT_TOKEN");

      this.login();
   }

   @Override
   public String cmd() {
      return "oc";
   }

   @Override
   public Proxy createProxy(String namespace, String podName) {
      return new OpenShiftProxy(namespace, podName, this);
   }

   private void login() {
      ProcessWrapper processWrapper = new ProcessWrapper();
      processWrapper.execute("oc login --token=%s --server=%s", this.token, this.url);
   }

   private String validateEnv(String name) {
      String value = System.getenv(name);
      if (value == null || value.trim().length() == 0) {
         throw new IllegalStateException("OpenShift env " + name + " cannot be empty");
      }
      return value;
   }

   @Override
   public String toString() {
      return "openshift";
   }
}
