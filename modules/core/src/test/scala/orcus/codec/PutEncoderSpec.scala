package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSpec

class PutEncoderSpec extends FunSpec {

  case class X(a: A, b: B, c: C)
  case class A(x: Int)
  case class B(y: String)
  case class C(z: Long)

  describe("apply") {
    it("should derive Put from case class") {
      val row = Bytes.toBytes("row")
      val p   = PutEncoder[X].apply(new Put(row), X(A(1), B("2"), C(3))).get
      assert(p.has(Bytes.toBytes("a"), Bytes.toBytes("x"), Bytes.toBytes(1)))
      assert(p.has(Bytes.toBytes("b"), Bytes.toBytes("y"), Bytes.toBytes("2")))
      assert(p.has(Bytes.toBytes("c"), Bytes.toBytes("z"), Bytes.toBytes(3L)))
    }
    it("should derive Put from Map") {
      val row = Bytes.toBytes("row")
      val p = PutEncoder[Map[String, A]]
        .apply(new Put(row), Map("a" -> A(1), "b" -> A(2), "c" -> A(3)))
        .get
      assert(p.has(Bytes.toBytes("a"), Bytes.toBytes("x"), Bytes.toBytes(1)))
      assert(p.has(Bytes.toBytes("b"), Bytes.toBytes("x"), Bytes.toBytes(2)))
      assert(p.has(Bytes.toBytes("c"), Bytes.toBytes("x"), Bytes.toBytes(3)))
    }
  }
}
