package orcus.builder

import java.{util => ju}

import org.apache.hadoop.hbase.client.{Durability, Increment}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

import scala.collection.JavaConverters._

class HIncrementSpec extends BuilderSpec {

  describe("withACL") {
    it("should call setACL") {
      val m = spy(new Increment(rowkey))
      val v = new Permission
      HIncrement.withACL("1", v).run(m)
      verify(m).setACL("1", v)
    }
  }
  describe("withCellVisibility") {
    it("should call setCellVisibility") {
      val m = spy(new Increment(rowkey))
      val v = new CellVisibility("!a")
      HIncrement.withCellVisibility(v).run(m)
      verify(m).setCellVisibility(v)
    }
  }
  describe("withDurability") {
    it("should call setDurability") {
      val m = spy(new Increment(rowkey))
      HIncrement.withDurability(Durability.ASYNC_WAL).run(m)
      verify(m).setDurability(Durability.ASYNC_WAL)
    }
  }
  describe("withId") {
    it("should call setId") {
      val m = spy(new Increment(rowkey))
      HIncrement.withId("1").run(m)
      verify(m).setId("1")
    }
  }
  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new Increment(rowkey))
      HIncrement.withAttribute("n", Bytes.toBytes("v")).run(m)
      verify(m).setAttribute("n", Bytes.toBytes("v"))
    }
  }
  describe("withClusterIds") {
    it("should call setClusterIds") {
      val m = spy(new Increment(rowkey))
      val v = ju.Arrays.asList(ju.UUID.randomUUID(), ju.UUID.randomUUID())
      HIncrement.withClusterIds(v.asScala).run(m)
      verify(m).setClusterIds(v)
    }
  }
  describe("withTTL") {
    it("should call setTTL") {
      val m = spy(new Increment(rowkey))
      HIncrement.withTTL(100).run(m)
      verify(m).setTTL(100)
    }
  }
  describe("withColumn") {
    it("should call setColumn") {
      val m  = spy(new Increment(rowkey))
      val cf = Bytes.toBytes("a")
      HIncrement.withColumn(cf, "v", 100).run(m)
      verify(m).addColumn(cf, Bytes.toBytes("v"), 100)
    }
  }
}
