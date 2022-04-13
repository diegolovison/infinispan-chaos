package org.infinispan.chaos;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.infinispan.chaos.environment.Environment;
import org.infinispan.chaos.io.ProcessWrapper;

public class ChaosTestingFailure {

   private final ScheduledExecutorService executor;
   private final String file;
   private final Environment environment;
   private final String namespace;

   public ChaosTestingFailure(ScheduledExecutorService executor, String file, Environment environment, String namespace) {
      this.executor = executor;
      this.file = file;
      this.environment = environment;
      this.namespace = namespace;
   }

   public void solveAfter(long delay, TimeUnit unit) {
      executor.schedule(() -> {
         ProcessWrapper process = new ProcessWrapper();
         try {
            process.start(String.format("%s delete -f %s -n %s", this.environment.cmd(), file, this.namespace));
            // x delete
            process.read();
            process.destroy();
         } catch (Exception e) {
            // silent
         }
      }, delay, unit);
   }
}
