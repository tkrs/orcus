package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.flatspec.AnyFlatSpec

class PutEncoderSpec extends AnyFlatSpec {
  case class X(a: A, b: B, c: C, d: D)
  case class A(x: Int)
  case class B(y: String)
  case class C(z: Long)
  case class D(w: Option[Long])

  it should "encode to Put from Map" in {
    val row = Bytes.toBytes("row")
    val ts  = Long.MaxValue
    val map = Map("a" -> Map(1 -> 0), "b" -> Map(2 -> 1), "c" -> Map(3 -> 1))
    val p   = PutEncoder[Map[String, Map[Int, Int]]].apply(new Put(row), map)
    assert(p.has(Bytes.toBytes("a"), Bytes.toBytes(1), ts, Bytes.toBytes(0)))
    assert(p.has(Bytes.toBytes("b"), Bytes.toBytes(2), ts, Bytes.toBytes(1)))
    assert(p.has(Bytes.toBytes("c"), Bytes.toBytes(3), ts, Bytes.toBytes(1)))
  }
}
