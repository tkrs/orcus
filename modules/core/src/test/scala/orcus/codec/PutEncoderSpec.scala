package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSpec

class PutEncoderSpec extends FunSpec {
  import generic.derived._

  case class X(a: A, b: B, c: C, d: D)
  case class A(x: Int)
  case class B(y: String)
  case class C(z: Long)
  case class D(w: Option[Long])

  describe("apply") {
    it("should derive Put from case class") {
      val row = Bytes.toBytes("row")
      val ts  = Long.MaxValue
      val x   = X(A(1), B("2"), C(3), D(None))
      val p   = PutEncoder[X].apply(new Put(row), x)
      assert(p.has(Bytes.toBytes("a"), Bytes.toBytes("x"), ts, Bytes.toBytes(1)))
      assert(p.has(Bytes.toBytes("b"), Bytes.toBytes("y"), ts, Bytes.toBytes("2")))
      assert(p.has(Bytes.toBytes("c"), Bytes.toBytes("z"), ts, Bytes.toBytes(3L)))
      assert(p.has(Bytes.toBytes("d"), Bytes.toBytes("w"), ts, Array.ofDim[Byte](0)))
    }
    it("should derive Put from Map") {
      val row = Bytes.toBytes("row")
      val ts  = Long.MaxValue
      val map = Map("a" -> A(1), "b" -> A(2), "c" -> A(3))
      val p   = PutEncoder[Map[String, A]].apply(new Put(row), map)
      assert(p.has(Bytes.toBytes("a"), Bytes.toBytes("x"), ts, Bytes.toBytes(1)))
      assert(p.has(Bytes.toBytes("b"), Bytes.toBytes("x"), ts, Bytes.toBytes(2)))
      assert(p.has(Bytes.toBytes("c"), Bytes.toBytes("x"), ts, Bytes.toBytes(3)))
    }
  }
}
