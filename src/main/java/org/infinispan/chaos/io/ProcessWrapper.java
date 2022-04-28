package org.infinispan.chaos.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessWrapper {

   private static final Logger log = LogManager.getLogger(ProcessWrapper.class);

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
      log.info(String.format("Output line from process(%d): %s", process.pid(), line));
   }

   public void destroy() {
      long pid = process.pid();
      log.info(String.format("Destroying process(%d)", pid));
      if (process != null) {
         process.destroy();
         process.destroyForcibly();
      }
      log.info(String.format("Waiting to exit process(%d)", pid));
      try {
         process.onExit().get();
         log.info(String.format("Exit process(%d): %d", pid, process.exitValue()));
      } catch (InterruptedException e) {
         throw new IllegalStateException(e);
      } catch (ExecutionException e) {
         throw new IllegalStateException(e);
      }
   }

   public void execute(String command, String... args) {
      try {
         start(String.format(command, args));
      } catch (IOException e) {
         throw new IllegalStateException("Cannot execute: " + command, e);
      }
      while (true) {
         try {
            if (!(reader.readLine() != null)) break;
         } catch (IOException e) {
            throw new IllegalStateException("Cannot read process", e);
         }
      }
      destroy();
   }
}
