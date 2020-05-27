package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.flatspec.AnyFlatSpec

class PutFamilyEncoderSpec extends AnyFlatSpec {
  it should "encode to Put from family's Map" in {
    val row = Bytes.toBytes("row")
    val cf = Bytes.toBytes("cf")
    val ts = 1L
    val map = Map("a" -> 1, "b" -> 2, "c" -> 3)
    val p = PutFamilyEncoder[Map[String, Int]].apply(new Put(row, ts), cf, map)
    assert(p.has(cf, Bytes.toBytes("a"), ts, Bytes.toBytes(1)))
    assert(p.has(cf, Bytes.toBytes("b"), ts, Bytes.toBytes(2)))
    assert(p.has(cf, Bytes.toBytes("c"), ts, Bytes.toBytes(3)))
  }
}
