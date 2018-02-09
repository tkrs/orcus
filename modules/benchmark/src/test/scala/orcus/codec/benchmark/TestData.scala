package orcus.codec.benchmark

import java.{util => ju}

import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.{Cell, CellBuilderType, ExtendedCellBuilderFactory}
import org.apache.hadoop.hbase.util.Bytes

import scala.collection.JavaConverters._

final case class Table(
    cf1: Columns
)

final case class Columns(a: Option[Int] = None,
                         b: Option[Float] = None,
                         c: Option[Long] = None,
                         d: Option[Double] = None,
                         e: Option[String] = None,
                         g: Option[Boolean] = None,
                         h: Option[Short] = None,
                         i: Option[BigDecimal] = None)

class TestData {
  val row: Array[Byte] = Bytes.toBytes("row")
  val cf1: Array[Byte] = Bytes.toBytes("cf1")

  def cell(q: String, v: Array[Byte]): Cell = {
    val builder = ExtendedCellBuilderFactory.create(CellBuilderType.DEEP_COPY)
    builder
      .setRow(row)
      .setFamily(cf1)
      .setQualifier(Bytes.toBytes(q))
      .setTimestamp(Long.MaxValue)
      .setType(Cell.Type.Put)
      .setValue(v)
      .build()
  }

  val cells: ju.List[Cell] = Seq(
    cell("a", Bytes.toBytes(1)),
    cell("b", Bytes.toBytes(1.1f)),
    cell("c", Bytes.toBytes(100L)),
    cell("d", Bytes.toBytes(1.9)),
    cell("e", Bytes.toBytes("s")),
    cell("g", Bytes.toBytes(true)),
    cell("h", Bytes.toBytes(Short.MaxValue)),
    cell("i", Bytes.toBytes(BigDecimal(10).bigDecimal))
  ).asJava

  val result: Result = Result.create(cells)
}
