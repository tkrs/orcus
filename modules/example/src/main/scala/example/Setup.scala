package example

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.TableName

object Setup {
  val tableName: TableName          = TableName.valueOf("Hello")
  val columnFamilyName: Array[Byte] = Bytes.toBytes("cf1")
  val columnName1: Array[Byte]      = Bytes.toBytes("greeting1")
  val columnName2: Array[Byte]      = Bytes.toBytes("greeting2")
  val greeting: String              = "Hello World!"
}
