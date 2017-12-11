package orcus.builder

import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}

trait BuilderSpec extends FunSpec with MockitoSugar with Matchers {
  val rowkey: Array[Byte] = Bytes.toBytes("KEY")
}
