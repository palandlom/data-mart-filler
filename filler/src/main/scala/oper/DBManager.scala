package oper

import com.typesafe.scalalogging.LazyLogging

import java.sql.{Connection, DriverManager, ResultSet, SQLException}
import conf.DatabaseConf

object DBManager extends LazyLogging {

  // load and register JDBC driver for MySQL
  Class.forName("org.postgresql.Driver")

  //  val conn_str = f"jdbc:mysql://localhost:3306/DBNAME?user=DBUSER&password=DBPWD"
  val connectionStr = f"jdbc:postgresql://" +
    f"${DatabaseConf.addr}:" +
    f"${DatabaseConf.port}/" +
    f"${DatabaseConf.dbName}?" +
    f"user=${DatabaseConf.user}&&password=${DatabaseConf.pass}"


  def executeWriteQuery(query: String): Unit = {
    val conn: Option[Connection] = try {
      Some(DriverManager.getConnection(connectionStr))
    } catch {
      case e: Throwable =>
        logger.error(f"can't connect to db ${e.getMessage}")
        Option.empty[Connection]
    }

    conn.foreach(c => {
      val stm = c.createStatement()
      try {
        stm.executeUpdate(query)
      } catch {
        case e: SQLException =>
          logger.error(f"can't execute query ${e.getMessage}")
          Option.empty[ResultSet]
      } finally {
        c.close()
      }
    })
  }
}
