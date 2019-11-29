package orcus.codec.generic

import orcus.codec.PutFamilyEncoder
import orcus.codec.semiauto._
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FlatSpec

class DerivedPutFamilyEncoderSpec extends FlatSpec {
  case class Foo(a: Int, b: String, c: Double)

  object Foo {
    implicit val encode: PutFamilyEncoder[Foo] = derivedPutFamilyEncoder[Foo]
  }

  it should "encode to Put from case class" in {
    val row = Bytes.toBytes("row")
    val cf  = Bytes.toBytes("cf")
    val ts  = 1L
    val a   = Foo(1, "2", 3.0)
    val p   = PutFamilyEncoder[Foo].apply(new Put(row, ts), cf, a)

    assert(p.has(cf, Bytes.toBytes("a"), ts, Bytes.toBytes(1)))
    assert(p.has(cf, Bytes.toBytes("b"), ts, Bytes.toBytes("2")))
    assert(p.has(cf, Bytes.toBytes("c"), ts, Bytes.toBytes(3.0)))
  }
}
