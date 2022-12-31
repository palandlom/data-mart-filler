package oper


import com.typesafe.scalalogging.LazyLogging
import conf.DatabaseConf
import datamodels.RawNewsDTO
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import scala.collection.convert.ImplicitConversions.`list asScalaBuffer`
import scala.io.Source
import scala.util.{Failure, Success, Try}
import conf.SparkConf.{sparkContext, sparkSession}

import java.sql.SQLIntegrityConstraintViolationException


//import  org.apache.spark.sql.sources.
// This import is needed to use the $-notation


object RawDataProcessor extends LazyLogging {

  var nextPortionCounter: Int = -1
  var portionFilePath = ".nextPortionNum"
  val UNKNOWN_CATEGORY = "_unknown_category_"

  val categoryMappingDF = sparkSession.read
    .format("jdbc")
    .option("url", DatabaseConf.Url())
    .option("dbtable", f"${DatabaseConf.categoryMappingTableName}")
    .option("user", DatabaseConf.user)
    .option("password", DatabaseConf.pass)
    .load()

  val newsDF = sparkSession.read
    .format("jdbc")
    .option("url", DatabaseConf.Url())
    .option("dbtable", f"${DatabaseConf.newsTableName}")
    .option("user", DatabaseConf.user)
    .option("password", DatabaseConf.pass)
    .load()

  val uncategorizedNewsDF = sparkSession.read
    .format("jdbc")
    .option("url", DatabaseConf.Url())
    .option("dbtable", f"${DatabaseConf.uncategorizedNewsTableName}")
    .option("user", DatabaseConf.user)
    .option("password", DatabaseConf.pass)
    .load()


  this.newsDF.createOrReplaceTempView(DatabaseConf.newsTableName)
  this.categoryMappingDF.createOrReplaceTempView(DatabaseConf.categoryMappingTableName)
  this.uncategorizedNewsDF.createOrReplaceTempView(DatabaseConf.uncategorizedNewsTableName)

  def getNextPortionNum(): Int = {
    if (this.nextPortionCounter < 0) {
      // try to read from file
      this.nextPortionCounter = Try {
        val lines = Source.fromFile(portionFilePath).getLines.toList
        lines.head.toInt
      } match {
        case Success(i) => i
        case Failure(s) => 0
      }
    }
    this.nextPortionCounter
  }

  def setNextPortionNum(num: Int): Unit = {
    Utiler.printToFile(new File(this.portionFilePath)) { writer => writer.println(num) }
    this.nextPortionCounter = num
  }


  def cleanAndUploadRawData(rawNews: Seq[RawNewsDTO]): Unit = {
    val categoryMapping = getExistingCategoryMapping()
    logger.info(f" Get category mapping - size ${categoryMapping.size}")

    val categorizedNews = rawNews.groupBy(n => getCanonicalCategory(n, categoryMapping))
    logger.info(f" Categorized news - size ${categorizedNews.values.map{var s = 0; d => {s += d.size; s}}}")

    this.insertNewsToTables(categorizedNews)
  }

  def processUncategorizedNews(): Unit = {
    logger.info(f"processUncategorizedNews started")
    import sparkSession.implicits._
    val sdf: SimpleDateFormat = new SimpleDateFormat(conf.DatabaseConf.timestampFormatString, Locale.getDefault)

    val uncategorizedNews = this.uncategorizedNewsDF.map(row => {
      val timestamp = Utiler.TimestampToString(row.get(3), sdf)
      timestamp match {
        case None => {
          logger.error(f"can't cast Timestamp to string - skip uncategorized news hash:[${row.get(0)}]")
          Option.empty[RawNewsDTO]
        }
        case _ => {
          val categories = Utiler.ParseJsonArray(row.getString(4))
          if (categories.isEmpty) {
            logger.error(f"can't parse categories - skip uncategorized news hash:[${row.get(0)}]")
            Option.empty
          }
          else Some(RawNewsDTO(
            Categories = categories.get,
            Title = row.getString(1),
            Url = row.getString(2),
            PublishedDateTime = timestamp.get))
        }
      }
    }).filter(!_.isEmpty).map(e => e.get).collect().toSeq



    val categoryMapping = getExistingCategoryMapping()
    val categorizedNews = uncategorizedNews.groupBy(n => getCanonicalCategory(n, categoryMapping))
    val processedNewsHashes = insertNewsToTables(categorizedNews)
    deleteFromUncategorizedNewsTable(processedNewsHashes)
    logger.info(f"Get ${uncategorizedNews.size} uncategorizedNews for processing - ${processedNewsHashes.size} were categorized")
    logger.info(f"processUncategorizedNews finished")
  }


  private def insertNewsToTables(canonicalCategoryToNewsDTOs: Map[String, Seq[RawNewsDTO]]): Array[String] = {

    var addedNewsHashes = Array[String]()
    canonicalCategoryToNewsDTOs foreach { case (ctg, newsDTOs) =>
      newsDTOs.foreach(news => {

        val query = ctg match {
          case UNKNOWN_CATEGORY => {
            this.insertUnknownCategoriesIntoTable(news.Categories)
            val categoriesJsonStr = Utiler.SeqToJsonArrayString(news.Categories)
            f"INSERT INTO ${DatabaseConf.uncategorizedNewsTableName} " +
              f"(news_hash, title, url, published_datetime, categories) " +
              f"VALUES ('${news.md5()}', '${news.Title}', '${news.Url}', CAST('${news.PublishedDateTime}' AS timestamp), '${categoriesJsonStr}');"
          }
          case _ => f"INSERT INTO ${DatabaseConf.newsTableName} " +
            f"(news_hash, title, url, published_datetime, canonical_category) " +
            f"VALUES ('${news.md5()}', '${news.Title}', '${news.Url}', CAST('${news.PublishedDateTime}' AS timestamp), '$ctg');"
        }

        try {
          sparkSession.sql(query)
          addedNewsHashes = addedNewsHashes :+ news.md5() // if no exception then add to hashes of added news
        } catch {
          // if duplicate key exception then add this news to hashes of added news - it is already added
          case e: SQLIntegrityConstraintViolationException => addedNewsHashes = addedNewsHashes :+ news.md5()
          case e: Exception => None
        }

      })
    }
    addedNewsHashes
  }

  private def insertUnknownCategoriesIntoTable(unknownCategories: Seq[String]): Unit = {
    unknownCategories.map(_.toLowerCase)
      .foreach(ctg => {
        val query = f"INSERT INTO ${DatabaseConf.categoryMappingTableName} " +
          f"(raw_category_hash, raw_category, canonical_category) " +
          f"VALUES ('${Utiler.md5(ctg)}', '${ctg}', '');"

        try {
          sparkSession.sql(query)
        } catch {
          case e: Exception => None
        }
      })
  }

  /**
   * Delete news from UncategorizedNewsTable by given hashes. Deletion is performed
   * by non-spark way
   *
   * @param newsHashes
   */
  private def deleteFromUncategorizedNewsTable(newsHashes: Seq[String]): Unit = {
    import sparkSession.implicits._
    val sqlHashStrings = uncategorizedNewsDF.filter(row => newsHashes.contains(row.get(0).toString))
      .map(row => row.getString(0))
      .map(hash => f"'$hash'").collect()

    val sqlHashString = sqlHashStrings.size match {
      case 0 => ""
      case 1 => sqlHashStrings(0)
      case _ => sqlHashStrings.mkString(",")
    }

    if (!sqlHashString.isEmpty) {
      val query = f"DELETE FROM ${conf.DatabaseConf.uncategorizedNewsTableName} WHERE news_hash IN ($sqlHashString);"
      DBManager.executeWriteQuery(query)
    }
  }

  /**
   * Read mapping from db-table.
   * @return map{raw_category : canonical_category}
   */
  private def getExistingCategoryMapping() =
    categoryMappingDF.select("raw_category", "canonical_category").collectAsList()
      .filter(row => row.get(1).toString.trim.size > 0) // row with empty canonical category shouldn't participate in mapping
      .map(row => {
        row.get(0).toString -> row.get(1).toString
      })
      .toMap

  /**
   * For given news returns its canonical category or UNKNOWN_CATEGORY
   * @param news
   * @param categoryMapping {raw_cat: canonical_cat}
   * @return
   */
  private def getCanonicalCategory(news: RawNewsDTO, categoryMapping: Map[String, String]): String = {
    val foundCanonicalCategories = news.Categories
      .filter(newsCtg => categoryMapping.contains(newsCtg.toLowerCase()))
      .map(newsCtg => categoryMapping.get(newsCtg.toLowerCase()))

    if (foundCanonicalCategories.size > 0) {
      foundCanonicalCategories.head.get
    } else {
      UNKNOWN_CATEGORY
    }
  }


}



