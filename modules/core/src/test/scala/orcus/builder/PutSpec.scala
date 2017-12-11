package orcus.builder

import java.{util => ju}

import org.apache.hadoop.hbase.client.{Durability, Put => HPut}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

import scala.collection.JavaConverters._

class PutSpec extends BuilderSpec {

  describe("withACL") {
    it("should call setACL") {
      val m = spy(new HPut(rowkey))
      val v = new Permission
      new Put(m).withACL("1", v)
      verify(m).setACL("1", v)
    }
  }
  describe("withCellVisibility") {
    it("should call setCellVisibility") {
      val m = spy(new HPut(rowkey))
      val v = new CellVisibility("!a")
      new Put(m).withCellVisibility(v)
      verify(m).setCellVisibility(v)
    }
  }
  describe("withDurability") {
    it("should call setDurability") {
      val m = spy(new HPut(rowkey))
      new Put(m).withDurability(Durability.ASYNC_WAL)
      verify(m).setDurability(Durability.ASYNC_WAL)
    }
  }
  describe("withId") {
    it("should call setId") {
      val m = spy(new HPut(rowkey))
      new Put(m).withId("1")
      verify(m).setId("1")
    }
  }
  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new HPut(rowkey))
      new Put(m).withAttribute("n", Bytes.toBytes("v"))
      verify(m).setAttribute("n", Bytes.toBytes("v"))
    }
  }
  describe("withClusterIds") {
    it("should call setClusterIds") {
      val m = spy(new HPut(rowkey))
      val v = ju.Arrays.asList(ju.UUID.randomUUID(), ju.UUID.randomUUID())
      new Put(m).withClusterIds(v.asScala)
      verify(m).setClusterIds(v)
    }
  }
  describe("withTTL") {
    it("should call setTTL") {
      val m = spy(new HPut(rowkey))
      new Put(m).withTTL(100)
      verify(m).setTTL(100)
    }
  }
  describe("withColumn") {
    it("should call addColumn") {
      val m  = spy(new HPut(rowkey))
      val cf = Bytes.toBytes("a")
      new Put(m).withColumn(cf, "b", "c")
      verify(m).addColumn(cf, Bytes.toBytes("b"), Bytes.toBytes("c"))
    }
  }
  describe("withColumnVersion") {
    it("should call addColumn") {
      val m  = spy(new HPut(rowkey))
      val cf = Bytes.toBytes("a")
      new Put(m).withColumnVersion(cf, "b", 1, "c")
      verify(m).addColumn(cf, Bytes.toBytes("b"), 1, Bytes.toBytes("c"))
    }
  }
  describe("withImmutable") {
    it("should call addImmutable") {
      val m  = spy(new HPut(rowkey))
      val cf = Bytes.toBytes("a")
      new Put(m).withImmutable(cf, "b", "c")
      verify(m).addImmutable(cf, Bytes.toBytes("b"), Bytes.toBytes("c"))
    }
  }
  describe("withImmutableVersion") {
    it("should call addImmutable") {
      val m  = spy(new HPut(rowkey))
      val cf = Bytes.toBytes("a")
      new Put(m).withImmutableVersion(cf, "b", 1, "c")
      verify(m).addImmutable(cf, Bytes.toBytes("b"), 1, Bytes.toBytes("c"))
    }
  }
}
