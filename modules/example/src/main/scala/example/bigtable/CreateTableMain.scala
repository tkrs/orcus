package example.bigtable

import com.google.cloud.bigtable.hbase.{BigtableConfiguration, BigtableOptionsFactory}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HColumnDescriptor, HTableDescriptor, TableName}

import scala.util.{Success, Try}

object setup {
  import BigtableOptionsFactory._

  val projectId  = sys.props("project-id")
  val instanceId = sys.props("instance-id")
  val emulator   = sys.props.contains("emulator")
  val config     = BigtableConfiguration.configure(projectId, instanceId)
  if (emulator) config.setBoolean(BIGTABLE_USE_PLAINTEXT_NEGOTIATION, true)

  val tableName: TableName          = TableName.valueOf("Hello")
  val columnFamilyName: Array[Byte] = Bytes.toBytes("cf1")
  val columnName1: Array[Byte]      = Bytes.toBytes("greeting1")
  val columnName2: Array[Byte]      = Bytes.toBytes("greeting2")
  val greeting: String              = "Hello World!"

}

object CreateTableMain extends App {
  import setup._
  import Functions._

  def run(getConnection: => Connection): Unit = {
    bracket(Try(getConnection)) { connection =>
      val tableDescriptor  = new HTableDescriptor(tableName)
      val columnDescriptor = new HColumnDescriptor(columnFamilyName)
      columnDescriptor.setTimeToLive(1)
      tableDescriptor.addFamily(columnDescriptor)

      bracket(getAdmin(connection)) { admin =>
        for {
          a <- Try(admin.tableExists(tableName))
          _ <- if (a) Try(admin.deleteTable(tableName)) else Success(())
          _ <- Try(admin.createTable(tableDescriptor))
        } yield ()
      }

    }.get
  }

  run(BigtableConfiguration.connect(config))
}

object DeleteTableMain extends App {
  import setup._
  import Functions._

  def run(getConnection: => Connection): Unit = {
    bracket(Try(getConnection)) { connection =>
      bracket(getAdmin(connection)) { admin =>
        for {
          a <- Try(admin.tableExists(tableName))
          _ <- Try(admin.deleteTable(tableName)) if a
        } yield ()
      }

    }.getOrElse(println("Table is not exists"))
  }

  run(BigtableConfiguration.connect(config))
}
