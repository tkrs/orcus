package orcus.bigtable.codec

import com.google.cloud.bigtable.data.v2.models.RowCell
import com.google.protobuf.ByteString
import orcus.bigtable.CRow
import orcus.bigtable.codec.semiauto._
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.funsuite.AnyFunSuite

class RowDecoderTest extends AnyFunSuite {
  case class Foo(c1: Bar, c2: Option[Baz], c3: Option[Baz] = None)

  object Foo {
    implicit val decode: RowDecoder[Foo] = derivedRowDecoder[Foo]
  }
  case class Bar(a: Int, b: String, c: Option[Double])

  object Bar {
    implicit val decode: FamilyDecoder[Bar] = derivedFamilyDecoder[Bar]
  }
  case class Baz(d: Long, e: Boolean, f: Float, g: Option[Int] = None, h: Option[Long] = None)

  object Baz {
    implicit val decode: FamilyDecoder[Baz] = derivedFamilyDecoder[Baz]
  }

  test("decodeFoo") {
    val ts = System.currentTimeMillis() * 1000L
    val row = CRow(
      "rowkey",
      Map(
        "c1" -> List(
          RowCell.create(
            "c1",
            ByteString.copyFromUtf8("a"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(10))
          ),
          RowCell.create(
            "c1",
            ByteString.copyFromUtf8("b"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes("string"))
          ),
          RowCell.create(
            "c1",
            ByteString.copyFromUtf8("c"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(10.999))
          )
        ),
        "c2" -> List(
          RowCell.create(
            "c2",
            ByteString.copyFromUtf8("d"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(101L))
          ),
          RowCell.create(
            "c2",
            ByteString.copyFromUtf8("e"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(true))
          ),
          RowCell.create(
            "c2",
            ByteString.copyFromUtf8("f"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(10.555f))
          )
        )
      )
    )

    val expected = Foo(
      Bar(10, "string", Some(10.999)),
      Some(Baz(101L, true, 10.555f))
    )

    assert(RowDecoder[Foo].apply(row) === Right(expected))
  }

  test("decodeOption") {
    val ts = System.currentTimeMillis() * 1000L
    val row = CRow(
      "rowkey",
      Map(
        "c1" -> List(
          RowCell.create(
            "c1",
            ByteString.copyFromUtf8("a"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(10))
          ),
          RowCell.create(
            "c1",
            ByteString.copyFromUtf8("b"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes("string"))
          )
        ),
        "c3" -> List(
          RowCell.create(
            "c3",
            ByteString.copyFromUtf8("d"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(101L))
          ),
          RowCell.create(
            "c3",
            ByteString.copyFromUtf8("e"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(true))
          ),
          RowCell.create(
            "c3",
            ByteString.copyFromUtf8("f"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(10.555f))
          ),
          RowCell.create(
            "c3",
            ByteString.copyFromUtf8("h"),
            ts,
            java.util.List.of(),
            ByteString.EMPTY
          )
        )
      )
    )

    val expected = Foo(Bar(10, "string", None), None, Some(Baz(101L, true, 10.555f, None, None)))

    assert(RowDecoder[Foo].apply(row) === Right(expected))
  }

  test("decodeMap") {
    val ts = System.currentTimeMillis() * 1000L
    val row = CRow(
      "rowkey",
      Map(
        "c1" -> List(
          RowCell.create(
            "c1",
            ByteString.copyFrom(Bytes.toBytes(10)),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(100.toShort))
          ),
          RowCell.create(
            "c1",
            ByteString.copyFrom(Bytes.toBytes(11)),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(110.toShort))
          )
        ),
        "c2" -> List(
          RowCell.create(
            "c1",
            ByteString.copyFrom(Bytes.toBytes(12)),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(120.toShort))
          )
        )
      )
    )

    val expected = Map(
      "c1" -> Map(
        10 -> 100.toShort,
        11 -> 110.toShort
      ),
      "c2" -> Map(
        12 -> 120.toShort
      )
    )

    assert(RowDecoder[Map[String, Map[Int, Short]]].apply(row) === Right(expected))
  }
}
