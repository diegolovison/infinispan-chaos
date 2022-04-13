package org.infinispan.chaos.exception;

public class ChaosTestingException extends RuntimeException {

   public ChaosTestingException(Exception root) {
      super(root);
   }

   public ChaosTestingException(String message) {
      super(message);
   }
}
