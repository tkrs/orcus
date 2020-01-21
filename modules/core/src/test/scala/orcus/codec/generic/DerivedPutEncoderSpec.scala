package orcus.codec.generic

import orcus.codec.{PutEncoder, PutFamilyEncoder}
import orcus.codec.semiauto._
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.flatspec.AnyFlatSpec

class DerivedPutEncoderSpec extends AnyFlatSpec {
  case class X(a: A, b: B, c: C, d: D)

  object X {
    implicit val encode: PutEncoder[X] = derivedPutEncoder[X]
  }

  case class A(x: Int)

  object A {
    implicit val encode: PutFamilyEncoder[A] = derivedPutFamilyEncoder[A]
  }

  case class B(y: String)

  object B {
    implicit val encode: PutFamilyEncoder[B] = derivedPutFamilyEncoder[B]
  }

  case class C(z: Long)

  object C {
    implicit val encode: PutFamilyEncoder[C] = derivedPutFamilyEncoder[C]
  }

  case class D(w: Option[Long])

  object D {
    implicit val encode: PutFamilyEncoder[D] = derivedPutFamilyEncoder[D]
  }

  it should "encode to Put from case class" in {
    val row = Bytes.toBytes("row")
    val ts  = Long.MaxValue
    val x   = X(A(1), B("2"), C(3), D(None))
    val p   = PutEncoder[X].apply(new Put(row), x)

    assert(p.has(Bytes.toBytes("a"), Bytes.toBytes("x"), ts, Bytes.toBytes(1)))
    assert(p.has(Bytes.toBytes("b"), Bytes.toBytes("y"), ts, Bytes.toBytes("2")))
    assert(p.has(Bytes.toBytes("c"), Bytes.toBytes("z"), ts, Bytes.toBytes(3L)))
    assert(p.has(Bytes.toBytes("d"), Bytes.toBytes("w"), ts, Array.ofDim[Byte](0)))
  }
}
