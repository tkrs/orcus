package orcus.builder

import java.{util => ju}

import org.apache.hadoop.hbase.client.{Durability, Increment => HIncrement}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

import scala.collection.JavaConverters._

class IncrementSpec extends BuilderSpec {

  describe("withACL") {
    it("should call setACL") {
      val m = spy(new HIncrement(rowkey))
      val v = new Permission
      new Increment(m).withACL("1", v)
      verify(m).setACL("1", v)
    }
  }
  describe("withCellVisibility") {
    it("should call setCellVisibility") {
      val m = spy(new HIncrement(rowkey))
      val v = new CellVisibility("!a")
      new Increment(m).withCellVisibility(v)
      verify(m).setCellVisibility(v)
    }
  }
  describe("withDurability") {
    it("should call setDurability") {
      val m = spy(new HIncrement(rowkey))
      new Increment(m).withDurability(Durability.ASYNC_WAL)
      verify(m).setDurability(Durability.ASYNC_WAL)
    }
  }
  describe("withId") {
    it("should call setId") {
      val m = spy(new HIncrement(rowkey))
      new Increment(m).withId("1")
      verify(m).setId("1")
    }
  }
  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new HIncrement(rowkey))
      new Increment(m).withAttribute("n", Bytes.toBytes("v"))
      verify(m).setAttribute("n", Bytes.toBytes("v"))
    }
  }
  describe("withClusterIds") {
    it("should call setClusterIds") {
      val m = spy(new HIncrement(rowkey))
      val v = ju.Arrays.asList(ju.UUID.randomUUID(), ju.UUID.randomUUID())
      new Increment(m).withClusterIds(v.asScala)
      verify(m).setClusterIds(v)
    }
  }
  describe("withTTL") {
    it("should call setTTL") {
      val m = spy(new HIncrement(rowkey))
      new Increment(m).withTTL(100)
      verify(m).setTTL(100)
    }
  }
  describe("withColumn") {
    it("should call setColumn") {
      val m = spy(new HIncrement(rowkey))
      new Increment(m).withColumn("cf", "v", 100)
      verify(m).addColumn(Bytes.toBytes("cf"), Bytes.toBytes("v"), 100)
    }
  }
}
