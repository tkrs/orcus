package orcus.bigtable

import com.google.cloud.bigtable.data.v2.models.RowCell
import com.google.protobuf.ByteString
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite

class RowDecoderTest extends FunSuite {

  case class Foo(c1: Bar, c2: Option[Baz])
  case class Bar(a: Int, b: String, c: Option[Double])
  case class Baz(d: Long, e: Boolean, f: Float, g: BigDecimal)

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
          ),
          RowCell.create(
            "c2",
            ByteString.copyFromUtf8("g"),
            ts,
            java.util.List.of(),
            ByteString.copyFrom(Bytes.toBytes(BigDecimal(89783692).bigDecimal))
          )
        )
      )
    )

    val expected = Foo(
      Bar(10, "string", Some(10.999)),
      Some(Baz(101L, true, 10.555f, BigDecimal(89783692)))
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
        )
      )
    )

    val expected = Foo(Bar(10, "string", None), None)

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
