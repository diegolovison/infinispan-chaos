package org.infinispan.chaos.query;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.infinispan.chaos.ChaosTesting;
import org.infinispan.chaos.hotrod.HotRodClient;
import org.infinispan.chaos.io.ProcessWrapper;
import org.infinispan.chaos.io.Sleep;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.junit.jupiter.api.Test;

import com.opencsv.bean.CsvToBeanBuilder;

public class QueryTest {

   @Test
   public void testStopServers() throws IOException {

      int numClients = 3;

      ChaosTesting chaosTesting = new ChaosTesting()
            .deploy("query_deploy.yaml", "query_config.yaml")
            .expectedNumClients(numClients);

      try {
         List<Movie> movies = readFromCsv();

         String cacheName = "cache-with-persistence";

         GeneratedSchema schema = new MovieSchemaBuilderImpl();
         ConfigurationBuilder builder = new ConfigurationBuilder();
         builder.addContextInitializer(schema);

         chaosTesting.run(cacheName, null, (poll) -> {
            try {
               HotRodClient client = poll.next();
               RemoteCache remoteCache = client.getRemoteCache();
               RemoteCache<String, String> metadataCache = remoteCache.getRemoteCacheManager().getCache(PROTOBUF_METADATA_CACHE_NAME);
               metadataCache.put(schema.getProtoFileName(), schema.getProtoFile());
               for (Movie movie : movies) {
                  remoteCache.put(movie.getId(), movie);
               }
               // 4756
               // Execute a full-text query
               QueryFactory queryFactory = Search.getQueryFactory(remoteCache);
               Query<Movie> query = queryFactory.create("FROM movie.Movie");

               List<Movie> list = query.execute().list(); // Voila! We have our book back from the cache!
               int remoteSize = client.getRemoteCache().size();
               int moviesSize = movies.size();
               // waiting an answer from dev the team if we could change cache container to `<metrics accurate-size="true"/>`
               /*
               if (remoteSize != moviesSize) {
                  throw new IllegalStateException("remoteSize != moviesSize");
               }
               */

               // restart the server
               for (int c = 0; c < 100; c++) {
                  for (int i = 0; i < numClients; i++) {
                     stopServer(i);
                     Sleep.sleep(30_000);
                     for (HotRodClient itClient : poll.getClients()) {
                        if (itClient.getRemoteCache().size() == 0) {
                           throw new IllegalStateException("itClient.getRemoteCache().size() == 0");
                        }
                     }
                  }
               }

               System.out.println();
            } catch (Exception e) {
               e.printStackTrace();
               chaosTesting.stop();
               throw new RuntimeException(e);
            }
         }, builder);
      } finally {
         chaosTesting.stop();
      }
   }

   private void stopServer(int index) throws IOException {
      ProcessWrapper process = new ProcessWrapper();
      String command = String.format("kubectl exec -it --namespace=ispn-testing example-infinispan-%d -- bash -c \"echo 'shutdown server' > bin/commands.txt\"", index);
      process.start(command);
      process.read();
      process.destroy();

      process = new ProcessWrapper();
      command = String.format("kubectl exec -it --namespace=ispn-testing example-infinispan-%d -- bash -c \"bin/cli.sh -c http://127.0.0.1:11222 -f bin/commands.txt\"", index);
      process.start(command);
      process.read();
      process.destroy();
   }

   private List<Movie> readFromCsv() throws FileNotFoundException {
      String fileName = QueryTest.class.getClassLoader().getResource("data/movies.csv").getFile();

      List<Movie> beans = new CsvToBeanBuilder(new FileReader(fileName))
            .withType(Movie.class)
            .build()
            .parse();

      Set<String> ids = new HashSet<>();

      for (Movie movie : beans) {
         if (!ids.add(movie.getId())) {
            throw new IllegalStateException("Duplicated id");
         }
      }

      return beans;
   }
}
