package oper

import java.sql.{DriverManager, ResultSet}

class dbManager {

  def initConnection(): Unit = {
    classOf[org.postgresql.Driver]
    val con_str = "jdbc:postgresql://localhost:5432/main?user=postgres&password=mysecretpassword"
//    mysecretpassword
    val conn = DriverManager.getConnection(con_str)
    try {
      val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

//      val rs = stm.executeQuery("SHOW DATABASE")

//      while (rs.next) {
//        println(rs.getString("quote"))
//      }
    } finally {
      conn.close()
    }
  }


}
