package conf

object AppConf {


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
//  val rawNewsFilesDirPath = "/newsFiles"

  // TODO can be red from from conffile
  val newsRssSources =
    "https://lenta.ru/rss/" ::
      "https://www.vedomosti.ru/rss/news" ::
      "https://tass.ru/rss/v2.xml" ::
      Nil

}
