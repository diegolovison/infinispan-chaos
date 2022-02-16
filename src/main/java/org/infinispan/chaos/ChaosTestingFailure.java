package org.infinispan.chaos;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.infinispan.chaos.io.ProcessWrapper;

public class ChaosTestingFailure {

   private final ScheduledExecutorService executor;
   private final String file;

   public ChaosTestingFailure(ScheduledExecutorService executor, String file) {
      this.executor = executor;
      this.file = file;
   }

   public void solveAfter(long delay, TimeUnit unit) {
      executor.schedule(() -> {
         ProcessWrapper process = new ProcessWrapper();
         try {
            process.start("kubectl delete -f " + file);
            // x delete
            process.read();
            process.destroy();
         } catch (Exception e) {
            // silent
         }
      }, delay, unit);
   }
}
