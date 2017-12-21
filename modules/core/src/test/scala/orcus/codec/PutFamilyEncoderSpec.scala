package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSpec

class PutFamilyEncoderSpec extends FunSpec {

  case class A(a: Int, b: String, c: Double)

  describe("apply") {
    it("should derive Put from case class") {
      val row = Bytes.toBytes("row")
      val cf  = Bytes.toBytes("cf")
      val p   = PutCFEncoder[A].apply(new Put(row), cf, A(1, "2", 3.0)).get
      assert(p.has(cf, Bytes.toBytes("a"), Bytes.toBytes(1)))
      assert(p.has(cf, Bytes.toBytes("b"), Bytes.toBytes("2")))
      assert(p.has(cf, Bytes.toBytes("c"), Bytes.toBytes(3.0)))
    }
  }
}
