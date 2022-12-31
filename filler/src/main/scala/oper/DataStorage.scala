package oper

import com.typesafe.scalalogging.LazyLogging
import datamodels.RawNewsDTO
import net.liftweb.json.Serialization.write
import net.liftweb.json._

import java.io._
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import java.util.concurrent.ConcurrentHashMap
import scala.io.Source
import scala.jdk.CollectionConverters.ConcurrentMapHasAsScala
import conf.AppConf

object DataStorage extends LazyLogging {

  private var collectedRawNews: ConcurrentHashMap[String, RawNewsDTO] = new ConcurrentHashMap

  def addNews(news: Seq[RawNewsDTO]): Unit = {
    for (n <- news) {
      this.collectedRawNews.put(n.hashCode().toString, n)
    }
  }

  def newsQuantity(): Int = {
    this.collectedRawNews.size()
  }

  /**
   * Drop all content in memory to files and clean memory-storage.
   *
   * @return
   */
  def dropNews(): String = {

    // chose unused file number //TODO consider change index to timestamp
    val filenameBase = "news"
    var i = 0

    val getFileName = (index: Int, base: String) => {
      f"${index}_${base}"
    }

    var filepath = Paths.get(AppConf.rawNewsFilesDirPath, getFileName(i, filenameBase))
    while (Files.exists(filepath)) {
      i += 1
      filepath = Paths.get(AppConf.rawNewsFilesDirPath, getFileName(i, filenameBase))
    }

    // Write news to file as json-strings + collect dropped newsId
    val droppedNewIds = this.collectedRawNews.asScala.keySet
    implicit val formats = DefaultFormats
    Utiler.printToFile(new File(filepath.toString)) { writer =>
      droppedNewIds.foreach(newsId => {

        val newsJsonStr = write(this.collectedRawNews.get(newsId))
        writer.println(newsJsonStr) // write json as usual string
      })
    }
    logger.info(f"NewsStorage.dropNews() finished - [${droppedNewIds.size}] were saved to [${filepath.toString}]")

    // Remove dropped news from memory storage
    droppedNewIds.foreach(newsId => {
      this.collectedRawNews.remove(newsId)
    })

    filepath.toString
  }


  /**
   * Returns raw nes from portions with nums more than given one.
   * @param portionNum
   * @return
   */

  /**
   *
   * @param portionNum
   * @return news, quantity of processed files
   */
  def getNewsPortionsFrom(portionNum: Int): (Seq[RawNewsDTO], Int) = {

    val getPortionNumFromFileName = (filename: String) => {
      val parts = filename.split("_")
      if (parts.size>1) {
        Utiler.toInt(parts(0))
      } else {
        None
      }
    }
    val filesToLoad = Utiler.getListOfFiles(AppConf.rawNewsFilesDirPath)
      .filter(f => {
        val pnum = getPortionNumFromFileName(f.getName)
        (!pnum.isEmpty && pnum.get >= portionNum)
      })

    val news = filesToLoad.flatMap(f => Source.fromFile(f, UTF_8.toString).getLines()).map(line => {
      implicit val formats = DefaultFormats
      parse(line).extract[RawNewsDTO]
    })

    (news,filesToLoad.size)
  }

  /**
   * @deprecated not used
   * @param portionNums
   * @return
   */
  def getNewsPortions(portionNums: Int*): Seq[RawNewsDTO] = {
    // TODO get all files
    val filesToLoad = Utiler.getListOfFiles(AppConf.rawNewsFilesDirPath)
      .filter(f => {
        var i = 0
        var isFound = false
        while (i < portionNums.size) {
          if (f.getName.startsWith(f"${portionNums(i)}_")) {
            isFound = true
            i = portionNums.size
          }
        }
        isFound
      })

    filesToLoad.flatMap(f => Source.fromFile(f, UTF_8.toString).getLines()).map(line => {
      implicit val formats = DefaultFormats
      parse(line).extract[RawNewsDTO]
    })
  }


}
