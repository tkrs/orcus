package orcus.builder

import java.{util => ju}

import org.apache.hadoop.hbase.client.{Durability, Append => HAppend}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

import scala.collection.JavaConverters._

class AppendSpec extends BuilderSpec {
  describe("withId") {
    it("should call setId") {
      val m = spy(new HAppend(rowkey))
      new Append(m).withId("1")
      verify(m).setId("1")
    }
  }

  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new HAppend(rowkey))
      val v = Bytes.toBytes("value")
      new Append(m).withAttribute("1", v)
      verify(m).setAttribute("1", v)
    }
  }
  describe("withClusterIds") {
    it("should call setClusterIds") {
      val m = spy(new HAppend(rowkey))
      val v = ju.Arrays.asList(ju.UUID.randomUUID(), ju.UUID.randomUUID())
      new Append(m).withClusterIds(v.asScala)
      verify(m).setClusterIds(v)
    }
  }
  describe("withTTL") {
    it("should call setTTL") {
      val m = spy(new HAppend(rowkey))
      new Append(m).withTTL(10000000)
      verify(m).setTTL(10000000)
    }
  }
  describe("withValue") {

    it("should call add") {
      val m  = spy(new HAppend(rowkey))
      val cf = Bytes.toBytes("1")
      new Append(m).withValue(cf, "2", 3)
      verify(m).add(cf, Bytes.toBytes("2"), Bytes.toBytes(3))
    }
  }
  describe("withReturnResults") {

    it("should call setReturnResults") {
      val m = spy(new HAppend(rowkey))
      new Append(m).withReturnResults(true)
      verify(m).setReturnResults(true)
    }
  }
  describe("withACL") {

    it("should call setACL") {
      val m = spy(new HAppend(rowkey))
      val v = new Permission
      new Append(m).withACL("1", v)
      verify(m).setACL("1", v)
    }
  }
  describe("withCellVisibility") {

    it("should call setCellVisibility") {
      val m = spy(new HAppend(rowkey))
      val v = new CellVisibility("!a")
      new Append(m).withCellVisibility(v)
      verify(m).setCellVisibility(v)
    }
  }
  describe("withDurability") {

    it("should call setDurability") {
      val m = spy(new HAppend(rowkey))
      new Append(m).withDurability(Durability.ASYNC_WAL)
      verify(m).setDurability(Durability.ASYNC_WAL)
    }
  }
}
