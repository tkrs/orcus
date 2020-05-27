package orcus.codec

import org.apache.hadoop.hbase.util.Bytes
import org.scalacheck.{Arbitrary, Prop, Shrink}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.Checkers

class ValueCodecSpec extends AnyFunSuite with Checkers with Matchers {
  private def _roundTrip[A: ValueCodec](a: A): Boolean = {
    val encoded = ValueCodec[A].encode(a)
    val decoded = ValueCodec[A].decode(encoded)
    decoded === Right(a)
  }

  private def roundTrip[A: ValueCodec: Arbitrary: Shrink] =
    check(Prop.forAll(_roundTrip[A](_)))

  test("ValueCodec[Boolean]")(roundTrip[Boolean])
  test("ValueCodec[Short]")(roundTrip[Short])
  test("ValueCodec[Int]")(roundTrip[Int])
  test("ValueCodec[Long]")(roundTrip[Long])
  test("ValueCodec[Float]")(roundTrip[Float])
  test("ValueCodec[Double]")(roundTrip[Double])
  test("ValueCodec[BigDecimal]")(roundTrip[BigDecimal])
  test("ValueCodec[String]")(roundTrip[String])
  test("ValueCodec[Option[Int]]")(roundTrip[Option[Int]])

  test("ValueCodec[Option[String] should decode empty bytes as empty string") {
    assert(ValueCodec[Option[String]].decode(Array.emptyByteArray) === Right(Some("")))
  }

  test("ValueCodec[Option[String] should decode null as None") {
    assert(ValueCodec[Option[String]].decode(null) === Right(None))
  }

  test("ValueCodec[Option[A]] should decode null as None") {
    assert(ValueCodec[Option[Boolean]].decode(null) === Right(None))
  }

  test("imap") {
    val f = ValueCodec[Int].imap[String](_.toInt, _.toString)
    val v0 = f.decode(Bytes.toBytes(10))
    val v1 = f.encode("10")
    assert(v0 === Right("10"))
    assert(Bytes.toInt(v1) === 10)
  }
}
