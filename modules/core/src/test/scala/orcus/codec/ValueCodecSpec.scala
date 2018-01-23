package orcus.codec

import org.apache.hadoop.hbase.util.Bytes
import org.scalacheck.{Arbitrary, Prop, Shrink}
import org.scalatest.prop.Checkers
import org.scalatest.{FunSuite, Matchers}

class ValueCodecSpec extends FunSuite with Checkers with Matchers {

  private def _roundTrip[A: ValueCodec](a: A): Boolean = {
    val encoded = ValueCodec[A].encode(Option(a))
    val decoded = ValueCodec[A].decode(encoded)
    decoded === Option(a)
  }

  private def roundTripNone[A: ValueCodec]: Boolean = {
    val a       = Option.empty[A]
    val encoded = ValueCodec[A].encode(a)
    val decoded = ValueCodec[A].decode(encoded)
    decoded === Option(a)
  }

  private def roundTrip[A: ValueCodec: Arbitrary: Shrink] =
    check(Prop.forAll((a: A) => _roundTrip[A](a)))

  test("ValueCodec[Boolean]")(roundTrip[Boolean])
  test("ValueCodec[Short]")(roundTrip[Short])
  test("ValueCodec[Int]")(roundTrip[Int])
  test("ValueCodec[Long]")(roundTrip[Long])
  test("ValueCodec[Float]")(roundTrip[Float])
  test("ValueCodec[Double]")(roundTrip[Double])
  test("ValueCodec[BigDecimal]")(roundTrip[BigDecimal])
  test("ValueCodec[String]")(roundTrip[String])
  test("ValueCodec[Option[A]]")(roundTrip[Option[Int]])

  test("ValueCodec[Boolean]None")(roundTripNone[Boolean])
  test("ValueCodec[Short]None")(roundTripNone[Short])
  test("ValueCodec[Int]None")(roundTripNone[Int])
  test("ValueCodec[Long]None")(roundTripNone[Long])
  test("ValueCodec[Float]None")(roundTripNone[Float])
  test("ValueCodec[Double]None")(roundTripNone[Double])
  test("ValueCodec[BigDecimal]None")(roundTripNone[BigDecimal])
  test("ValueCodec[String]None")(roundTripNone[String])
  test("ValueCodec[Option[A]]None")(roundTripNone[Option[Int]])

  test("imap") {
    val f        = ValueCodec[Int].imap[String](_.toInt, _.toString)
    val Some(v0) = f.decode(Bytes.toBytes(10))
    val v1       = f.encode(Some("10"))
    assert(v0 === "10")
    assert(Bytes.toInt(v1) === 10)
  }
}
