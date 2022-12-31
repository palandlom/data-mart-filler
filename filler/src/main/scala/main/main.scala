package main


import com.typesafe.scalalogging.LazyLogging
import oper.RawDataProcessor.processUncategorizedNews
import oper.{RawDataExtractor, RawDataProcessor}
import conf.AppConf

import scala.::


object Main extends App with LazyLogging {

  val rawDataExtractor = new RawDataExtractor
  // === function for collecting news from sources and saving them in file
  val getAllProvidedNewsCase = () => {
    AppConf.newsRssSources.map(urlStr => {
      val collectNewsThread = new CollectNewsThread(rawDataExtractor, urlStr)
      collectNewsThread.start()
      collectNewsThread
    }).foreach(th => th.join()) // waiting for each collection-thread to be completed
    oper.DataStorage.dropNews()
  }


  // === function for upload news from files to db
  val preprocessAndUploadCase = () => {
    logger.info(f"PreprocessAndUpload started")
    val (rawNews, portionQty) = oper.DataStorage.getNewsPortionsFrom(RawDataProcessor.getNextPortionNum())
    oper.RawDataProcessor.cleanAndUploadRawData(rawNews)
    oper.RawDataProcessor.setNextPortionNum(RawDataProcessor.getNextPortionNum() + portionQty)
    logger.info(f"PreprocessAndUpload finished. [${rawNews.size}] were processed")
  }

  // === function for performCategoryAnalysis
  val performCategoryAnalysis = () => {
    logger.info(f"PerformCategoryAnalysis started")
    oper.DataAnalyser.performAnalysisOfNewsAndFormDataMart()
    logger.info(f"PerformCategoryAnalysis finished")
  }


  (if (args.size==0) "" else args(0)) match {
    // === Get all possible news (last 24 hours)
    case AppConf.getAllArg => getAllProvidedNewsCase()
    case AppConf.preprocessAndUploadArg => preprocessAndUploadCase() //prep
    case AppConf.processUncategorizedNewsArg => processUncategorizedNews() //uncat
    case AppConf.performCategoryAnalysisArg => performCategoryAnalysis() //anl
//    case AppConf.getDeltaArg => println(s"not implemented")
    case _ => println(f"Use one of the following args: " +
      f"${AppConf.getAllArg} " +
      f"${AppConf.preprocessAndUploadArg} " +
      f"${AppConf.processUncategorizedNewsArg} " +
      f"${AppConf.performCategoryAnalysisArg}"
    )
  }


}


/**
 * Thread for collecting new from given rss-url by newsCollector.
 * Obtained news will be added to global DataStorage
 *
 * @param newsCollector
 * @param urlStr
 */
class CollectNewsThread(newsCollector: RawDataExtractor, urlStr: String) extends Thread with LazyLogging {
  override def run() {

    logger.info(f"CollectNewsThread [$urlStr] started")
    val news = newsCollector.collectNews(urlStr)
    oper.DataStorage.addNews(news)
    logger.info(f"CollectNewsThread [$urlStr] finished. News obtained/total=[${news.size}/${oper.DataStorage.newsQuantity()}]")
  }
}


