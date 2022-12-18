package oper

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{BooleanType, IntegerType, StringType, StructField, StructType, TimestampType}
import org.apache.spark.sql.functions.to_timestamp


object RawDataLoader {

  var processedPortionCounter: Int = -1

  // есть Н-файлов + пронумерованных
  // нужно загрузить счетчик следующего файла из файла nextFileNumber +
  // если нет -> начать с 0 + сохранить 0счетчик в файл
  //
  //  загрузили файл + почистили его
  //
  // грузим в бд в таблицу NEWS
  // загрузили - обновили мчетчик

  def getLastProcessedPortionNum(): Int = {
    if (this.processedPortionCounter>0) {
      this.processedPortionCounter
    } else {
      // load counter OR create portionCounterFile with 0
      0 // TODO
    }
  }

  def setLastProcessedPortionNum(num: Int): Unit = {
    // TODO save it for file
    this.processedPortionCounter = num
  }

  def cleanAndUploadRawData(): Unit = {

    val sparkConf = new SparkConf()
      .setAppName("dheeraj-spark-demo")
      .setMaster("local")

    val spark = SparkSession
      .builder()
      .config(sparkConf)
      .getOrCreate()

    val ctx = spark.sparkContext


  }




}
