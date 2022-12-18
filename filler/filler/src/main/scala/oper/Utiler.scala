package oper

import java.io.{File, FileReader}

object Utiler {

  /**
   * getListOfFiles(dir: String)
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
   * arg-function can use p as print-writer
   * Example:
   * val data = Array("Five", "strings", "in", "a", "file!")
   * printToFile(new File("example.txt")) { p =>
   * data.foreach(p.println)
   * }
   * TODO move func to utils
   *
   * @param f file to write
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

}
