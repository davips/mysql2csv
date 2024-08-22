import java.sql.{Connection, DriverManager}

case class Db(database: String) {
  val connection: Connection = try {
    val mysqlHost = "127.0.0.1"
    val mysqlPort = "33060"
    val mysqlPass = "1"
    val url = s"jdbc:mysql://$mysqlHost:$mysqlPort/" + database
    val connection = DriverManager.getConnection(url, "davi", mysqlPass)
//    println(s"Connection to $database opened.")
    connection
  } catch {
    case e: Throwable => //e.printStackTrace()
      sys.error(s"Problems opening db connection: ${e.getMessage} !")
  }

  def read(sql: String): List[Vector[Double]] = {
    try {
      val statement = connection.createStatement()
      val resultSet = statement.executeQuery(sql)
      val rsmd = resultSet.getMetaData
      val numColumns = rsmd.getColumnCount
      val columnsType = new Array[Int](numColumns + 1)
      columnsType(0) = 0
      1 to numColumns foreach (i => columnsType(i) = rsmd.getColumnType(i))
      val queue = collection.mutable.Queue[Seq[Double]]()
      while (resultSet.next()) {
        val seq = 1 to numColumns map { i => resultSet.getDouble(i) }
        queue.enqueue(seq)
      }
      resultSet.close()
      statement.close()
      queue.toList.map(_.toVector)
    } catch {
      case e: Throwable => //e.printStackTrace()
        sys.error(s"\nProblems executing SQL query '$sql': ${e.getMessage} .")
    }
  }

  def close(): Unit = {
    connection.close()
  }
}
