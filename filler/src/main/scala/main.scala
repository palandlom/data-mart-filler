import oper.{RawDataExtractor, RawDataProcessor}
import com.typesafe.scalalogging._
import conf.processUncategorizedNewsArg
import oper.RawDataProcessor.processUncategorizedNews


package object conf {
  /**
   * Get all news arg
   */
  val getAllArg = "all"

  /**
   * Get new news from prev run
   */
  val getDeltaArg = "delta"
  val preprocessAndUploadArg = "prep"
  val processUncategorizedNewsArg = "uncat"
  val performCategoryAnalysisArg = "anl"

  val rawNewsFilesDirPath = "./newsFiles"

  // TODO can be red from from conffile
  val newsRssSources =
    "https://lenta.ru/rss/" ::
      "https://www.vedomosti.ru/rss/news" ::
      "https://tass.ru/rss/v2.xml" ::
      Nil
}

object Main extends App with LazyLogging {

  val rawDataExtractor = new RawDataExtractor
  // === function for collecting news from sources and saving them in file
  val getAllProvidedNewsCase = () => {
    _root_.conf.newsRssSources.map(urlStr => {
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
//    oper.RawDataProcessor.setNextPortionNum(RawDataProcessor.getNextPortionNum()+portionQty) //TODO
    logger.info(f"PreprocessAndUpload finished. [${rawNews.size}] were uploaded")
  }

  // === function for performCategoryAnalysis
  val performCategoryAnalysis = () => {
    logger.info(f"PerformCategoryAnalysis started")
    oper.DataAnalyser.performAnalysisOfNewsAndFormDataMart()
    logger.info(f"PerformCategoryAnalysis finished")
  }


  args.toList(0) match {
    // === Get all possible news (last 24 hours)
    case _root_.conf.getAllArg => getAllProvidedNewsCase()
    case _root_.conf.preprocessAndUploadArg => preprocessAndUploadCase() //prep
    case _root_.conf.processUncategorizedNewsArg => processUncategorizedNews() //uncat
    case _root_.conf.performCategoryAnalysisArg => performCategoryAnalysis() //anl
    case _root_.conf.getDeltaArg => println(s"not implemented")
    case _ => println(f"Use one of the following arg: $getAllProvidedNewsCase $preprocessAndUploadCase")
  }



}





/**
 * Thread for collecting new from given rss-url by newsCollector.
 * Obtained news will be added to global DataStorage
 * @param newsCollector
 * @param urlStr
 */
class CollectNewsThread(newsCollector: RawDataExtractor, urlStr: String) extends Thread with LazyLogging
{
  override def run()
  {

    logger.info(f"CollectNewsThread [$urlStr] started")
    val news = newsCollector.collectNews(urlStr)
    oper.DataStorage.addNews(news)
    logger.info(f"CollectNewsThread [$urlStr] finished. News obtained/total=[${news.size}/${oper.DataStorage.newsQuantity()}]")
  }
}