package conf

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkConf {

  val sparkConf = new SparkConf()
    .setAppName("data-mart-filler")
    .setMaster("local")

  val sparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .config("spark.jars", "./sparkjars/postgresql-42.5.1.jar")
    .getOrCreate()

  val sparkContext = sparkSession.sparkContext

  sparkContext.setLogLevel("ERROR")


}
