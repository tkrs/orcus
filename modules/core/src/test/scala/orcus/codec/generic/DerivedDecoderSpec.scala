package orcus.codec.generic

import cats.syntax.option._
import orcus.codec.semiauto._
import orcus.codec.{CodecSpec, Decoder, FamilyDecoder}
import orcus.internal.Utils
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{Cell, CellBuilderType, ExtendedCellBuilderFactory}
import org.scalatest.flatspec.AnyFlatSpec

class DerivedDecoderSpec extends AnyFlatSpec with CodecSpec {
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

  object All {
    implicit val decodeAll: FamilyDecoder[All] = derivedFamilyDecoder[All]
  }
  case class Table0(cf1: All)

  object Table0 {
    implicit val decodeTable: Decoder[Table0] = derivedDecoder
  }

  it should "decode a nested case class" in {
    val f = Decoder[Table0]
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

    val cells = Utils.toJavaList(
      Seq(
        cell("a", Bytes.toBytes(1)),
        cell("b", Bytes.toBytes(1.1f)),
        cell("c", Bytes.toBytes(100L)),
        cell("d", Bytes.toBytes(1.9)),
        cell("e", Bytes.toBytes("s")),
        cell("g", Bytes.toBytes(true)),
        cell("h", Bytes.toBytes(Short.MaxValue)),
        cell("i", Bytes.toBytes(BigDecimal(10).bigDecimal))
      )
    )

    val result = Result.create(cells)
    val expected = Right(
      Table0(
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

  case class Foo(
    a: Short,
    b: String
  )

  object Foo {
    implicit val decode: FamilyDecoder[Foo] = derivedFamilyDecoder[Foo]
  }
  case class Table1(cf: Foo)

  object Table1 {
    implicit val decode: Decoder[Table1] = derivedDecoder[Table1]
  }

  it should "fail decode when the require property is absent" in {
    val f = Decoder[Table1]

    val cells = Utils.toJavaList(
      Seq(
        cell("row", "fc", "a", Bytes.toBytes(1)),
        cell("row", "fc", "b", Bytes.toBytes("1"))
      )
    )

    assert(f(Result.create(cells)).isLeft)
  }
}
