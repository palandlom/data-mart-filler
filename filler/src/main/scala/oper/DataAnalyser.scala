package oper

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.DoubleType

import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
//import org.apache.spark.sql.types.{BooleanType, IntegerType, StringType, StructField, StructType, TimestampType}
import conf.DatabaseConf
import conf.SparkConf.sparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType, DateType, TimestampType, LongType}
import scala.jdk.CollectionConverters._
import java.time.temporal.ChronoUnit


object DataAnalyser {

  /* === Datamart dataframe schemes ===*/
  var categoryStatSchema = StructType(
    StructField("canonical_category", StringType, true) ::
      // Общее количество новостей из всех источников по данной категории за все время
      StructField("news_total", LongType, true) ::
      // Среднее количество публикаций по данной категории в сутки
      StructField("news_average_per_day", DoubleType, true) ::
      // Общее количество новостей из всех источников по данной категории за последние сутки
      StructField("news_total_per_day", LongType, true) ::
      // День, в который было сделано максимальное количество новостей по данной категории
      StructField("max_news_day", DateType, true) ::
      Nil
  )

  var daysToCategoryStatSchema = StructType(
    // Количество публикаций новостей данной категории по дням недели
    StructField("canonical_category", StringType, true) ::
      StructField("day_of_week", StringType, true) ::
      StructField("news_quantity", LongType, true) ::
      Nil)

  var categoryToSourceStatSchema = StructType(
    StructField("source_id", StringType, true) ::
      StructField("canonical_category", StringType, true) ::
      // Количество новостей данной категории для каждого из источников за последние сутки
      StructField("news_total_for_last_day", LongType, true) ::
      // Количество новостей данной категории для каждого из источников за все время
      StructField("news_total_per_day", LongType, true) ::
      Nil)

  /**
   * Analyse news, form intermediate dataframes, join
   * them and write/overwrite data mart tables in database.
   */
  def performAnalysisOfNewsAndFormDataMart(): Unit = {git
    
    val newsDf = getNewsDataFrame()

    // === 1 categoryStat
    val newsWithPublicationDateDf = newsDf
      .withColumn("published_date",
        date_format(col("published_datetime"), "yyyy-MM-dd"))
    
    // Общее количество новостей из всех источников по данной категории за все время
    val newsTotalByCategoryDf = newsDf
      .groupBy("canonical_category").count()
      .select(col("canonical_category"), col("count").alias("news_total"))

    // Среднее количество публикаций по данной категории в сутки
    val newsAveragePerDayByCategory = newsWithPublicationDateDf
      .groupBy("canonical_category", "published_date")
      .count()
      .groupBy("canonical_category")
      .mean("count")
      .select(col("canonical_category"), col("avg(count)").alias("news_average_per_day"))

    // Общее количество новостей из всех источников по данной категории за последние сутки
    val lastDayDateInstant = Instant.now.minus(7, ChronoUnit.DAYS) // lastDayDateInstant = последние сутки
    val lastDayDate = Date.from(lastDayDateInstant)
    val formatter = new SimpleDateFormat(conf.DatabaseConf.dateFormatString)
    val lastDayDateStr = formatter.format(lastDayDate)
    val newsTotalLastDay = newsWithPublicationDateDf.select("*")
      .filter(col("published_date") === lastDayDateStr)
      .groupBy("canonical_category")
      .count()
      .select(col("canonical_category"), col("count").alias("news_total_last_day"))

    // День, в который было сделано максимальное количество новостей по данной категории
    // see https://sparkbyexamples.com/spark/spark-find-maximum-row-per-group-in-dataframe/
    val windowSpec = Window.partitionBy("canonical_category")
      .orderBy(col("news_qty").desc)
    val maxNewsDay = newsWithPublicationDateDf
      .groupBy("published_date", "canonical_category")
      .count()
      .select(col("published_date"), col("canonical_category"),
        col("count").alias("news_qty"))
      .withColumn("row_num", row_number.over(windowSpec))
      .filter(col("row_num") === 1).drop("row_num")
      .select(col("canonical_category"),
        to_date(col("published_date"),conf.DatabaseConf.dateFormatString).as("max_news_day"))

    // Join all intermediate DFs
    val categoryStatMartRawDf = newsTotalByCategoryDf
      .join(newsAveragePerDayByCategory, Seq("canonical_category"), "full")
      .join(newsTotalLastDay, Seq("canonical_category"),"full")
      .join(maxNewsDay, Seq("canonical_category"),"full")

    // Write dataMart DF to db
    val categoryStatMartDf = conf.SparkConf.sparkSession.createDataFrame(categoryStatMartRawDf.collect().toList.asJava,  categoryStatSchema)
    categoryStatMartDf.write.mode("overwrite")
      .format("jdbc")
      .option("url", DatabaseConf.Url())
      .option("dbtable", f"${DatabaseConf.categoryStatTableName}")
      .option("user", DatabaseConf.user)
      .option("password", DatabaseConf.pass)
      .save()


    // === 2 daysToCategoryStat
    // Количество публикаций новостей данной категории по дням недели
    val daysToCategoryStatSchemaMartRawDf = newsWithPublicationDateDf
      .withColumn("day_of_week", date_format(col("published_date"), "E"))
      .groupBy("canonical_category", "day_of_week").count()
      .sort(col("day_of_week").desc)

    daysToCategoryStatSchemaMartRawDf.show()
    // Write dataMart DF to db
    val daysToCategoryStatSchemaMartDf = conf.SparkConf.sparkSession.createDataFrame(daysToCategoryStatSchemaMartRawDf.collect().toList.asJava,  daysToCategoryStatSchema)
    daysToCategoryStatSchemaMartDf.write.mode("overwrite")
      .format("jdbc")
      .option("url", DatabaseConf.Url())
      .option("dbtable", f"${DatabaseConf.daysToCategoryStatTableName}")
      .option("user", DatabaseConf.user)
      .option("password", DatabaseConf.pass)
      .save()


    // === 3 categoryToSourceStat
    val hostExtractUdf = org.apache.spark.sql.functions.udf {
      (urlString: String) =>
        val url = new URL(urlString)
        url.getHost
    }
    val newsWithPublicationDateAndSourceDf = newsWithPublicationDateDf
      .withColumn("source", hostExtractUdf(col("url")))

    // Количество новостей данной категории для каждого из источников за последние сутки
    val ctgAndSourceNewsLastDayDf = newsWithPublicationDateAndSourceDf
      .filter(col("published_date") === lastDayDateStr)
      .groupBy("canonical_category", "source").count()
      .select(col("source"), col("canonical_category"),
        col("count").alias("news_total_for_last_day"))

    // Количество новостей данной категории для каждого из источников за все время
    val ctgAndSourceNewsDf = newsWithPublicationDateAndSourceDf
      .groupBy("canonical_category", "source").count()

    // Join all intermediate DFs
    val categoryToSourceStatSchemaMartRawDf = ctgAndSourceNewsLastDayDf
      .join(ctgAndSourceNewsDf, Seq("canonical_category"), "full")
      .join(newsTotalLastDay, Seq("canonical_category"), "full")
      .join(maxNewsDay, Seq("canonical_category"), "full")

    // Write dataMart DF to db
    val categoryToSourceStatSchemaMartDf = conf.SparkConf.sparkSession.createDataFrame(categoryToSourceStatSchemaMartRawDf.collect().toList.asJava, categoryToSourceStatSchema)
    categoryToSourceStatSchemaMartDf.write.mode("overwrite")
      .format("jdbc")
      .option("url", DatabaseConf.Url())
      .option("dbtable", f"${DatabaseConf.daysToCategoryStatTableName}")
      .option("user", DatabaseConf.user)
      .option("password", DatabaseConf.pass)
      .save()
  }

  /**
   * Read jdbc-table "news"
   * @return NewsDataFrame["news_hash", "title", "url", "published_datetime", "canonical_category"]
   */
  def getNewsDataFrame(): DataFrame = {
    val df = sparkSession.read.format("jdbc")
      .option("url", DatabaseConf.Url())
      .option("dbtable", f"${DatabaseConf.newsTableName}")
      .option("user", DatabaseConf.user)
      .option("password", DatabaseConf.pass)
      .load()

    df.createOrReplaceTempView(DatabaseConf.newsTableName)
    df
  }

}
