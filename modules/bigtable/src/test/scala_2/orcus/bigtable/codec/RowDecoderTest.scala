package orcus.bigtable.codec

import com.google.cloud.bigtable.data.v2.models.RowCell
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.protobuf.ByteString
import orcus.bigtable.CRow
import orcus.bigtable.codec.semiauto._
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
            "a".asBytes,
            ts,
            java.util.List.of(),
            10.asBytes
          ),
          RowCell.create(
            "c1",
            "b".asBytes,
            ts,
            java.util.List.of(),
            "string".asBytes
          ),
          RowCell.create(
            "c1",
            "c".asBytes,
            ts,
            java.util.List.of(),
            10.999.asBytes
          )
        ),
        "c2" -> List(
          RowCell.create(
            "c2",
            "d".asBytes,
            ts,
            java.util.List.of(),
            101L.asBytes
          ),
          RowCell.create(
            "c2",
            "e".asBytes,
            ts,
            java.util.List.of(),
            true.asBytes
          ),
          RowCell.create(
            "c2",
            "f".asBytes,
            ts,
            java.util.List.of(),
            10.555f.asBytes
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
            10.asBytes
          ),
          RowCell.create(
            "c1",
            "b".asBytes,
            ts,
            java.util.List.of(),
            "string".asBytes
          )
        ),
        "c3" -> List(
          RowCell.create(
            "c3",
            "d".asBytes,
            ts,
            java.util.List.of(),
            101L.asBytes
          ),
          RowCell.create(
            "c3",
            "e".asBytes,
            ts,
            java.util.List.of(),
            true.asBytes
          ),
          RowCell.create(
            "c3",
            "f".asBytes,
            ts,
            java.util.List.of(),
            10.555f.asBytes
          ),
          RowCell.create(
            "c3",
            "h".asBytes,
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
            10.asBytes,
            ts,
            java.util.List.of(),
            100.toShort.asBytes
          ),
          RowCell.create(
            "c1",
            11.asBytes,
            ts,
            java.util.List.of(),
            110.toShort.asBytes
          )
        ),
        "c2" -> List(
          RowCell.create(
            "c1",
            12.asBytes,
            ts,
            java.util.List.of(),
            120.toShort.asBytes
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

  implicit class BooleanOps(private val v: Boolean) {
    def asBytes: ByteString = ByteString.copyFrom(Array(if (v) -1 else 0).map(_.toByte))
  }
  implicit class ShortOps(private val v: Short) {
    def asBytes: ByteString = ByteString.copyFrom(Shorts.toByteArray(v))
  }
  implicit class IntOps(private val v: Int) {
    def asBytes: ByteString = ByteString.copyFrom(Ints.toByteArray(v))
  }
  implicit class LongOps(private val v: Long) {
    def asBytes: ByteString = ByteString.copyFrom(Longs.toByteArray(v))
  }
  implicit class FloatOps(private val v: Float) {
    def asBytes: ByteString = ByteString.copyFrom(Ints.toByteArray(java.lang.Float.floatToIntBits(v)))
  }
  implicit class DoubleOps(private val v: Double) {
    def asBytes: ByteString = ByteString.copyFrom(Longs.toByteArray(java.lang.Double.doubleToLongBits(v)))
  }
  implicit class StringOps(private val v: String) {
    def asBytes: ByteString = ByteString.copyFromUtf8(v)
  }
}
