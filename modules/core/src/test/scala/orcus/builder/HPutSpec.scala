package orcus.builder

import java.{util => ju}

import org.apache.hadoop.hbase.client.{Durability, Put}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

import scala.collection.JavaConverters._

class HPutSpec extends BuilderSpec {

  describe("withACL") {
    it("should call setACL") {
      val m = spy(new Put(rowkey))
      val v = new Permission
      HPut.withACL("1", v).run(m)
      verify(m).setACL("1", v)
    }
  }
  describe("withCellVisibility") {
    it("should call setCellVisibility") {
      val m = spy(new Put(rowkey))
      val v = new CellVisibility("!a")
      HPut.withCellVisibility(v).run(m)
      verify(m).setCellVisibility(v)
    }
  }
  describe("withDurability") {
    it("should call setDurability") {
      val m = spy(new Put(rowkey))
      HPut.withDurability(Durability.ASYNC_WAL).run(m)
      verify(m).setDurability(Durability.ASYNC_WAL)
    }
  }
  describe("withId") {
    it("should call setId") {
      val m = spy(new Put(rowkey))
      HPut.withId("1").run(m)
      verify(m).setId("1")
    }
  }
  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new Put(rowkey))
      HPut.withAttribute("n", Bytes.toBytes("v")).run(m)
      verify(m).setAttribute("n", Bytes.toBytes("v"))
    }
  }
  describe("withClusterIds") {
    it("should call setClusterIds") {
      val m = spy(new Put(rowkey))
      val v = ju.Arrays.asList(ju.UUID.randomUUID(), ju.UUID.randomUUID())
      HPut.withClusterIds(v.asScala).run(m)
      verify(m).setClusterIds(v)
    }
  }
  describe("withTTL") {
    it("should call setTTL") {
      val m = spy(new Put(rowkey))
      HPut.withTTL(100).run(m)
      verify(m).setTTL(100)
    }
  }
  describe("withColumn") {
    it("should call addColumn") {
      val m  = spy(new Put(rowkey))
      val cf = Bytes.toBytes("a")
      HPut.withColumn(cf, "b", "c").run(m)
      verify(m).addColumn(cf, Bytes.toBytes("b"), Bytes.toBytes("c"))
    }
  }
  describe("withColumnVersion") {
    it("should call addColumn") {
      val m  = spy(new Put(rowkey))
      val cf = Bytes.toBytes("a")
      HPut.withColumnVersion(cf, "b", 1, "c").run(m)
      verify(m).addColumn(cf, Bytes.toBytes("b"), 1, Bytes.toBytes("c"))
    }
  }
  describe("withImmutable") {
    it("should call addImmutable") {
      val m  = spy(new Put(rowkey))
      val cf = Bytes.toBytes("a")
      HPut.withImmutable(cf, "b", "c").run(m)
      verify(m).addImmutable(cf, Bytes.toBytes("b"), Bytes.toBytes("c"))
    }
  }
  describe("withImmutableVersion") {
    it("should call addImmutable") {
      val m  = spy(new Put(rowkey))
      val cf = Bytes.toBytes("a")
      HPut.withImmutableVersion(cf, "b", 1, "c").run(m)
      verify(m).addImmutable(cf, Bytes.toBytes("b"), 1, Bytes.toBytes("c"))
    }
  }
}
