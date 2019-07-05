package orcus.codec.generic

import cats.syntax.option._
import orcus.codec.{CodecSpec, Decoder}
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{Cell, CellBuilderType, ExtendedCellBuilderFactory}
import org.scalatest.FlatSpec

import scala.collection.JavaConverters._

class DerivedDecoderSpec extends FlatSpec with CodecSpec {
  import derived._

  it should "decode a nested case class" in {
    case class All(
      a: Option[Int] = None,
      b: Option[Float] = None,
      c: Option[Long] = None,
      d: Option[Double] = None,
      e: Option[String] = None,
      g: Option[Boolean] = None,
      h: Option[Short] = None,
      i: Option[BigDecimal] = None
    )
    case class Table(cf1: All)

    val f   = Decoder[Table]
    val row = Bytes.toBytes("row")
    val cf1 = Bytes.toBytes("cf1")

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

    val cells = Seq(
      cell("a", Bytes.toBytes(1)),
      cell("b", Bytes.toBytes(1.1f)),
      cell("c", Bytes.toBytes(100L)),
      cell("d", Bytes.toBytes(1.9)),
      cell("e", Bytes.toBytes("s")),
      cell("g", Bytes.toBytes(true)),
      cell("h", Bytes.toBytes(Short.MaxValue)),
      cell("i", Bytes.toBytes(BigDecimal(10).bigDecimal))
    ).asJava

    val result = Result.create(cells)
    val expected = Right(
      Table(
        cf1 = All(
          1.some,
          1.1f.some,
          100L.some,
          1.9.some,
          "s".some,
          true.some,
          Short.MaxValue.some,
          BigDecimal(10).some
        )
      )
    )

    assert(f(result) === expected)
  }

  it should "fail decode when the require property is absent" in {
    case class Foo(
      a: Short,
      b: String
    )
    case class Table(cf: Foo)

    val f = Decoder[Table]

    val cells = Seq(
      cell("row", "fc", "a", Bytes.toBytes(1)),
      cell("row", "fc", "b", Bytes.toBytes("1"))
    ).asJava

    assert(f(Result.create(cells)).isLeft)
  }
}
