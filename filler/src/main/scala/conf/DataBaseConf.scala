package conf


object DatabaseConf {



  // addr/alias docker-container with postgres-db of data-mart
//    var addr = "127.0.0.1"
  var addr = "filler-db"
  var port = "5432"
  var user = "postgres"
  var pass = "pass"
  var dbName = "news"

  var newsTableName = "news"
  var uncategorizedNewsTableName = "uncategorized_news"
  var categoryMappingTableName = "category_mapping"

  var  categoryStatTableName = "category_stat"
  var  daysToCategoryStatTableName = "days_to_category_stat"
  var  categoryToSourceStatTableName = "category_to_source_stat"

  val timestampFormatString = "yyyy-MM-dd HH:mm:ss.SSS Z"
  val dateFormatString = "yyyy-MM-dd"

  def Url(): String = {
    f"jdbc:postgresql://${this.addr}:${this.port}/${this.dbName}"
  }

}
