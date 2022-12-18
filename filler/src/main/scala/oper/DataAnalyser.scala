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

  /*
  Общее количество новостей из всех источников по данной категории за все время
  Среднее количество публикаций по данной категории в сутки
  Общее количество новостей из всех источников по данной категории за последние сутки
  День, в который было сделано максимальное количество публикаций по данной новости

  Количество публикаций новостей данной категории по дням недели

  Количество новостей данной категории для каждого из источников за все время
  Количество новостей данной категории для каждого из источников за последние сутки


  * */

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
      Nil)

  var categoryToSourceStatSchema = StructType(
    StructField("source_id", StringType, true) ::
      StructField("canonical_category", StringType, true) ::
      // Количество новостей данной категории для каждого из источников за последние сутки
      StructField("news_total_for_last_day", LongType, true) ::
      // Количество новостей данной категории для каждого из источников за все время
      StructField("news_total_per_day", LongType, true) ::
      Nil)


  var tmpScheme = StructType(
    StructField("source_id", StringType, true) :: Nil)


  def performCategoryAnalysis(): Unit = {

    //    var categoryToSourceStatDf = conf.SparkConf.sparkSession.createDataFrame(categoryToSourceStatSchema)
    //    https://stackoverflow.com/questions/74919573/schema-for-type-org-apache-spark-sql-types-datatype-is-not-supported/74920959#74920959
    //    var df = conf.SparkConf.sparkSession.createDataFrame(categoryToSourceStatTableName.asJava   , categoryStatSchema)
    //    df.createOrReplaceTempView(f"tmp_${conf.DatabaseConf.categoryStatSchema}")
    //    sparkSession.sql(f"drop table if exists ${conf.DatabaseConf.categoryToSourceStatTableName}")
    //    sparkSession.sql(f"create ${conf.DatabaseConf.categoryToSourceStatTableName} my_table as select * from tmp_${conf.DatabaseConf.categoryToSourceStatTableName}")

    //      df.write.
    //        option("mode", "overwrite").saveAsTable("example")

    //    df.write.mode("overwrite")
    //          .format("jdbc")
    //          .option("url", DatabaseConf.Url())
    //          .option("dbtable", f"${DatabaseConf.categoryToSourceStatTableName}")
    //          .option("user", DatabaseConf.user)
    //          .option("password", DatabaseConf.pass)
    //      .save()
    //      .saveAsTable("example")
    println("1>>>")

    //    , tmpScheme
    //val sqlContext = org.apache.spark.Se
    //    val sqlContext = new org.apache.spark.sql.SQLContext(sparkContext)

    //
    //    println("1>>>")
    //    categoryToSourceStatDf.createOrReplaceTempView(f"tmp_${conf.DatabaseConf.categoryToSourceStatTableName}")
    //    sparkSession.sql(f"drop table if exists ${conf.DatabaseConf.categoryToSourceStatTableName}")
    //    sparkSession.sql(f"create ${conf.DatabaseConf.categoryToSourceStatTableName} my_table as select * from tmp_${conf.DatabaseConf.categoryToSourceStatTableName}")
    //  println("2>>>")
    ""
    // === Ananasys
    val newsDf = getNewsDataFrame()

    // Общее количество новостей из всех источников по данной категории за все время
    val newsTotalByCategoryDf = newsDf
      .groupBy("canonical_category").count()
      .select(col("canonical_category"), col("count").alias("news_total"))

//    newsTotalByCategoryDf.show()

    // Среднее количество публикаций по данной категории в сутки
    val newsWithPublicationDateDf = newsDf
      .withColumn("published_date",
        date_format(col("published_datetime"), "yyyy-MM-dd"))



    val newsAveragePerDayByCategory = newsWithPublicationDateDf
      .groupBy("canonical_category", "published_date")
      .count()
      .groupBy("canonical_category")
      .mean("count")
      .select(col("canonical_category"), col("avg(count)").alias("news_average_per_day"))

//    newsAveragePerDayByCategory.show()

    // Общее количество новостей из всех источников по данной категории за последние сутки
    val lastDayDateInstant = Instant.now.minus(7, ChronoUnit.DAYS)
    val lastDayDate = Date.from(lastDayDateInstant)
    val formatter = new SimpleDateFormat(conf.DatabaseConf.dateFormatString)
    val lastDayDateStr = formatter.format(lastDayDate)
    val newsTotalLastDay = newsWithPublicationDateDf.select("*")
      .filter(col("published_date") === lastDayDateStr)
      .groupBy("canonical_category")
      .count()
      .select(col("canonical_category"), col("count").alias("news_total_last_day"))

newsTotalLastDay.show()


    // День, в который было сделано максимальное количество новостей по данной категории
    val windowSpec = Window.partitionBy("canonical_category")
      .orderBy(col("news_qty").desc)
    // see https://sparkbyexamples.com/spark/spark-find-maximum-row-per-group-in-dataframe/
    val maxNewsDay = newsWithPublicationDateDf
      .groupBy("published_date", "canonical_category")
      .count()
      .select(col("published_date"), col("canonical_category"),
        col("count").alias("news_qty"))
      .withColumn("row_num", row_number.over(windowSpec))
      .filter(col("row_num") === 1).drop("row_num")
//      .withColumn("max_news_day",col("published_date") )
//    to_date(col("published_date"),"MM-dd-yyyy").as("max_news_day")
      .select(col("canonical_category"), to_date(col("published_date"),conf.DatabaseConf.dateFormatString).as("max_news_day")) //2022-12-25
//      .select(col("canonical_category"), col("published_date").as("max_news_day"))

    maxNewsDay.show()

    val categoryStatDf = newsTotalByCategoryDf
      .join(newsAveragePerDayByCategory, Seq("canonical_category"), "full")
      .join(newsTotalLastDay, Seq("canonical_category"),"full")
      .join(maxNewsDay, Seq("canonical_category"),"full")


//    val newDataFrame = categoryStatDf.schema.fields.foldLeft(categoryStatDf) {
//      (df, s) => df.withColumn(s.name, df(s.name).cast(s.dataType))
//    }
//    var df = conf.SparkConf.sparkSession.createDataFrame(categoryStatDf   , categoryStatSchema)
    categoryStatDf.show()
    val rows = categoryStatDf.collect()
    val newDataFrame = conf.SparkConf.sparkSession.createDataFrame(rows.toList.asJava,  categoryStatSchema)

//    newDataFrame.show()

    newDataFrame.write.mode("overwrite")
      .format("jdbc")
      .option("url", DatabaseConf.Url())
      .option("dbtable", f"${DatabaseConf.categoryStatTableName}")
      .option("user", DatabaseConf.user)
      .option("password", DatabaseConf.pass)
      .save()


    //    newsTotalByCategoryDf newsAveragePerDayByCategory newsTotalLastDay maxNewsDay maxNewsDay

    // Вывести фамилии посетителей, которые читали хотя бы одну новость про спорт
    //    var joinedDf = (df.as("df").join(lkDf, col("user_id") === col("df.id")))
    //
    //    joinedDf.groupBy("user_id", "fio", "tag").count()
    //      .select("fio").filter(joinedDf("tag") === "Sport" && col("count") > 0).show()

    //    2============
    // Количество публикаций новостей данной категории по дням недели
    newsWithPublicationDateDf
      .withColumn("day_of_week", date_format(col("published_date"), "E"))
      .groupBy("canonical_category", "day_of_week").count()
      .sort(col("day_of_week").desc)


    // 3 =====
    // Количество новостей данной категории для каждого из источников за последние сутки
    val hostExtractUdf = org.apache.spark.sql.functions.udf {
      (urlString: String) =>
        val url = new URL(urlString)
        url.getHost
    }
    val newsWithPublicationDateAndSourceDf = newsWithPublicationDateDf
      .withColumn("source", hostExtractUdf(col("url")))


    val ctgAndSourceNewsLastDayDf = newsWithPublicationDateAndSourceDf
      .filter(col("published_date") === lastDayDateStr)
      .groupBy("canonical_category", "source").count()
      .select(col("source"), col("canonical_category"),
        col("count").alias("news_total_for_last_day"))

    //    ctgAndSourceNewsLastDayDf.show()

    // Количество новостей данной категории для каждого из источников за все время
    val ctgAndSourceNewsDf = newsWithPublicationDateAndSourceDf
      .groupBy("canonical_category", "source").count()

    //    ctgAndSourceNewsDf.show()
  }


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
