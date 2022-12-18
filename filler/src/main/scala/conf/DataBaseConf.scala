package conf


object DatabaseConf {


  var addr = "127.0.0.1"
  var port = "5432"
  var user = "postgres"
  var pass = "pass"
  var dbName = "news"

  var newsTableName = "news"
  var uncategorizedNewsTableName = "uncategorized_news"
  //    var categoryMappingTableName = "\"category_mapping\""
  var categoryMappingTableName = "category_mapping"



  //    .option("url", "jdbc:postgresql://127.0.0.1:5432/news")
  //    .option("dbtable", "\"raw_news\"")
  //    .option("user", "postgres")
  //    .option("password", "pass")

  var  categoryStatTableName = "category_stat_table"
  var  daysToCategoryStatTableName = "days_to_category_stat"
  var  categoryToSourceStatTableName = "category_to_source_stat"

  val timestampFormatString = "yyyy-MM-dd HH:mm:ss.SSS Z"
  val dateFormatString = "yyyy-MM-dd"

  def Url(): String = {
    f"jdbc:postgresql://${this.addr}:${this.port}/${this.dbName}"
  }

}
