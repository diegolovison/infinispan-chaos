package org.infinispan.chaos;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.infinispan.chaos.environment.Environment;
import org.infinispan.chaos.io.ProcessWrapper;

public class ChaosTestingFailure {

   private final ScheduledExecutorService executor;
   private final String file;
   private final Environment environment;

   public ChaosTestingFailure(ScheduledExecutorService executor, String file, Environment environment) {
      this.executor = executor;
      this.file = file;
      this.environment = environment;
   }

   public void solveAfter(long delay, TimeUnit unit) {
      executor.schedule(() -> {
         ProcessWrapper process = new ProcessWrapper();
         try {
            process.start(String.format("%s delete -f %s", this.environment.cmd(), file));
            // x delete
            process.read();
            process.destroy();
         } catch (Exception e) {
            // silent
         }
      }, delay, unit);
   }
}
