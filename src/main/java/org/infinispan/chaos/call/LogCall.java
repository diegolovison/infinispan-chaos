package org.infinispan.chaos.call;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;

public class LogCall {

   public static String call(CoreV1Api api, String namespace, String podName) {
      try {
         String log = api.readNamespacedPodLog(podName, namespace, null, null, null, null, null, null, null, null, null);
         return log;
      } catch (ApiException e) {
         return e.getResponseBody();
      }
   }
}
