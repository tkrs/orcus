package orcus.builder

import java.{util => ju}

import org.apache.hadoop.hbase.client.{Durability, Append}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

import scala.collection.JavaConverters._

class HAppendSpec extends BuilderSpec {
  describe("withId") {
    it("should call setId") {
      val m = spy(new Append(rowkey))
      HAppend.withId("1").run(m)
      verify(m).setId("1")
    }
  }

  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new Append(rowkey))
      val v = Bytes.toBytes("value")
      HAppend.withAttribute("1", v).run(m)
      verify(m).setAttribute("1", v)
    }
  }
  describe("withClusterIds") {
    it("should call setClusterIds") {
      val m = spy(new Append(rowkey))
      val v = ju.Arrays.asList(ju.UUID.randomUUID(), ju.UUID.randomUUID())
      HAppend.withClusterIds(v.asScala).run(m)
      verify(m).setClusterIds(v)
    }
  }
  describe("withTTL") {
    it("should call setTTL") {
      val m = spy(new Append(rowkey))
      HAppend.withTTL(10000000).run(m)
      verify(m).setTTL(10000000)
    }
  }
  describe("withValue") {

    it("should call add") {
      val m  = spy(new Append(rowkey))
      val cf = Bytes.toBytes("1")
      HAppend.withValue(cf, "2", 3).run(m)
      verify(m).add(cf, Bytes.toBytes("2"), Bytes.toBytes(3))
    }
  }
  describe("withReturnResults") {

    it("should call setReturnResults") {
      val m = spy(new Append(rowkey))
      HAppend.withReturnResults(true).run(m)
      verify(m).setReturnResults(true)
    }
  }
  describe("withACL") {

    it("should call setACL") {
      val m = spy(new Append(rowkey))
      val v = new Permission
      HAppend.withACL("1", v).run(m)
      verify(m).setACL("1", v)
    }
  }
  describe("withCellVisibility") {

    it("should call setCellVisibility") {
      val m = spy(new Append(rowkey))
      val v = new CellVisibility("!a")
      HAppend.withCellVisibility(v).run(m)
      verify(m).setCellVisibility(v)
    }
  }
  describe("withDurability") {

    it("should call setDurability") {
      val m = spy(new Append(rowkey))
      HAppend.withDurability(Durability.ASYNC_WAL).run(m)
      verify(m).setDurability(Durability.ASYNC_WAL)
    }
  }
}
