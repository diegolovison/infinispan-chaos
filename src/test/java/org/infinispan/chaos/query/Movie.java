package org.infinispan.chaos.query;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import com.opencsv.bean.CsvBindByPosition;

@Indexed
public class Movie {

   @CsvBindByPosition(position = 0)
   @ProtoField(number = 1)
   String budget;

   @CsvBindByPosition(position = 1)
   @ProtoField(number = 2)
   @Field(index= Index.YES, store = Store.NO)
   String id;

   @CsvBindByPosition(position = 2)
   @ProtoField(number = 3)
   String imdb_id;

   @CsvBindByPosition(position = 3)
   @ProtoField(number = 4)
   String original_language;

   @CsvBindByPosition(position = 4)
   @ProtoField(number = 5)
   String original_title;

   @CsvBindByPosition(position = 5)
   @ProtoField(number = 6)
   String overview;

   @CsvBindByPosition(position = 6)
   @ProtoField(number = 7)
   String popularity;

   @CsvBindByPosition(position = 7)
   @ProtoField(number = 8)
   String production_countries;

   @CsvBindByPosition(position = 8)
   @ProtoField(number = 9)
   String release_date;

   @CsvBindByPosition(position = 9)
   @ProtoField(number = 10)
   String revenue;

   @CsvBindByPosition(position = 10)
   @ProtoField(number = 11)
   String runtime;

   @CsvBindByPosition(position = 11)
   @ProtoField(number = 12)
   String status;

   @CsvBindByPosition(position = 12)
   @ProtoField(number = 13)
   String vote_average;

   @CsvBindByPosition(position = 13)
   @ProtoField(number = 14)
   String vote_count;

   @CsvBindByPosition(position = 14)
   @ProtoField(number = 15)
   String collectionName;

   @CsvBindByPosition(position = 15)
   @ProtoField(number = 16)
   String romance;

   @CsvBindByPosition(position = 16)
   @ProtoField(number = 17)
   String drama;

   @CsvBindByPosition(position = 17)
   @ProtoField(number = 18)
   String history;

   @CsvBindByPosition(position = 18)
   @ProtoField(number = 19)
   String thriller;

   @CsvBindByPosition(position = 19)
   @ProtoField(number = 20)
   String scienceFiction;

   @CsvBindByPosition(position = 20)
   @ProtoField(number = 21)
   String family;

   @CsvBindByPosition(position = 21)
   @ProtoField(number = 22)
   String fantasy;

   @CsvBindByPosition(position = 22)
   @ProtoField(number = 23)
   String action;

   @CsvBindByPosition(position = 23)
   @ProtoField(number = 24)
   String horror;

   @CsvBindByPosition(position = 24)
   @ProtoField(number = 25)
   String documentary;

   @CsvBindByPosition(position = 25)
   @ProtoField(number = 26)
   String TVMovie;

   @CsvBindByPosition(position = 26)
   @ProtoField(number = 27)
   String music;

   @CsvBindByPosition(position = 27)
   @ProtoField(number = 28)
   String war;

   @CsvBindByPosition(position = 28)
   @ProtoField(number = 29)
   String foreign;

   @CsvBindByPosition(position = 29)
   @ProtoField(number = 30)
   String crime;

   @CsvBindByPosition(position = 30)
   @ProtoField(number = 31)
   String western;

   @CsvBindByPosition(position = 31)
   @ProtoField(number = 32)
   String animation;

   @CsvBindByPosition(position = 32)
   @ProtoField(number = 33)
   String adventure;

   @CsvBindByPosition(position = 33)
   @ProtoField(number = 34)
   String mystery;

   @CsvBindByPosition(position = 34)
   @ProtoField(number = 35)
   String comedy;

   @CsvBindByPosition(position = 35)
   @ProtoField(number = 36)
   String homepagePresent;

   @CsvBindByPosition(position = 36)
   @ProtoField(number = 37)
   String z_score_popularity;

   @CsvBindByPosition(position = 37)
   @ProtoField(number = 38)
   String outliers;

   @CsvBindByPosition(position = 38)
   @ProtoField(number = 39)
   String producer1;

   @CsvBindByPosition(position = 39)
   @ProtoField(number = 40)
   String producer2;

   @CsvBindByPosition(position = 40)
   @ProtoField(number = 41)
   String totalLanguages;

   @CsvBindByPosition(position = 41)
   @ProtoField(number = 42)
   String profit;

   public Movie() {

   }

   @ProtoFactory
   public Movie(String budget, String id, String imdb_id, String original_language, String original_title, String overview, String popularity, String production_countries, String release_date, String revenue, String runtime, String status, String vote_average, String vote_count, String collectionName, String romance, String drama, String history, String thriller, String scienceFiction, String family, String fantasy, String action, String horror, String documentary, String TVMovie, String music, String war, String foreign, String crime, String western, String animation, String adventure, String mystery, String comedy, String homepagePresent, String z_score_popularity, String outliers, String producer1, String producer2, String totalLanguages, String profit) {
      this.budget = budget;
      this.id = id;
      this.imdb_id = imdb_id;
      this.original_language = original_language;
      this.original_title = original_title;
      this.overview = overview;
      this.popularity = popularity;
      this.production_countries = production_countries;
      this.release_date = release_date;
      this.revenue = revenue;
      this.runtime = runtime;
      this.status = status;
      this.vote_average = vote_average;
      this.vote_count = vote_count;
      this.collectionName = collectionName;
      this.romance = romance;
      this.drama = drama;
      this.history = history;
      this.thriller = thriller;
      this.scienceFiction = scienceFiction;
      this.family = family;
      this.fantasy = fantasy;
      this.action = action;
      this.horror = horror;
      this.documentary = documentary;
      this.TVMovie = TVMovie;
      this.music = music;
      this.war = war;
      this.foreign = foreign;
      this.crime = crime;
      this.western = western;
      this.animation = animation;
      this.adventure = adventure;
      this.mystery = mystery;
      this.comedy = comedy;
      this.homepagePresent = homepagePresent;
      this.z_score_popularity = z_score_popularity;
      this.outliers = outliers;
      this.producer1 = producer1;
      this.producer2 = producer2;
      this.totalLanguages = totalLanguages;
      this.profit = profit;
   }

   public String getId() {
      return id;
   }

   @Override
   public String toString() {
      return "Movie{" +
            "id='" + id + '\'' +
            '}';
   }
}
