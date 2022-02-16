package org.infinispan.chaos.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

public class ProcessWrapper {

   private Process process;
   private BufferedReader reader;
   private BufferedReader error;

   public void start(String command) throws IOException {
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command("bash", "-c", command);
      process = processBuilder.start();
      reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
      error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
   }

   public void read() throws IOException {
      String line = reader.readLine();
      System.out.println(line);
      if (line != null) {
         return;
      } else {
         line = error.readLine();
         System.err.println(line);
      }
   }

   public void destroy() {
      if (process != null) {
         process.destroy();
         process.destroyForcibly();
      }
      System.out.println("Wait to exit");
      try {
         process.onExit().get();
         System.out.println("Exit: " + process.exitValue());
      } catch (InterruptedException e) {
         throw new IllegalStateException(e);
      } catch (ExecutionException e) {
         throw new IllegalStateException(e);
      }
   }
}
