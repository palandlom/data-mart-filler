package oper

import net.liftweb.json.Serialization.write
import net.liftweb.json._

import java.io.{File, FileReader}
import java.math.BigInteger
import java.security.MessageDigest
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Utiler {

  /**
   * getListOfFiles(dir: String)
   *
   * @param dir
   * @return
   */
  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }


  /**
   * Open file + create writer for it then call arg-function and close file.
   * arg-function can use variable p as print-writer
   *
   * Example:
   * <p>
   * val data = Array("Five", "strings", "in", "a", "file!")
   * printToFile(new File("example.txt")) { p =>
   * data.foreach(p.println)
   * }
   *
   * @param f  file to write
   * @param op function that write something to file
   */
  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  def readFile(f: java.io.File)(op: java.io.Reader => Unit) {
    val p = new FileReader(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }


  /**
   *
   * @param s
   * @return
   * @see <a href="https://alvinalexander.com/source-code/scala-method-create-md5-hash-of-string/">
   */
  def md5(s: String) = {
    val bigInt = new BigInteger(1, MessageDigest.getInstance("MD5").digest(s.getBytes))
    val hashedValue = bigInt.toString(16).trim
    "%1$32s".format(hashedValue).replace(' ', '0')

  }

  def SeqToJsonArrayString (seq: Seq[String]) = {
    implicit val formats = DefaultFormats
    write(seq)
  }

  def ParseJsonArray(jsonArraySting: String) = {
    implicit val formats = DefaultFormats
    parse(jsonArraySting).extract[List[String]]
  }

  def TimestampToString(timestamp: Any, sdf: SimpleDateFormat): Option[String] = {

    try {
      Some(sdf.format(timestamp))
    } catch {
      case e: Exception => Option.empty[String]
    }
  }

//  def TimestampToString(dateStr: String, sdf: SimpleDateFormat): Option[String] =
//  {
////    val df = DateTimeFormatter.ofPattern()
//    val dayOfWeek = LocalDate.parse(dateStr, sdf).getDayOfWeek
//  }



}