package orcus.builder

import java.{util => ju}

import org.apache.hadoop.hbase.client.{Durability, Delete}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

import scala.collection.JavaConverters._

class HDeleteSpec extends BuilderSpec {

  describe("withACL") {
    it("should call setACL") {
      val m = spy(new Delete(rowkey))
      val v = new Permission
      HDelete.withACL("1", v).run(m)
      verify(m).setACL("1", v)
    }
  }
  describe("withCellVisibility") {
    it("should call setCellVisibility") {
      val m = spy(new Delete(rowkey))
      val v = new CellVisibility("!a")
      HDelete.withCellVisibility(v).run(m)
      verify(m).setCellVisibility(v)
    }
  }
  describe("withClusterIds") {

    it("should call setClusterIds") {
      val m = spy(new Delete(rowkey))
      val v = ju.Arrays.asList(ju.UUID.randomUUID(), ju.UUID.randomUUID())
      HDelete.withClusterIds(v.asScala).run(m)
      verify(m).setClusterIds(v)
    }
  }
  describe("withDurability") {
    it("should call setDurability") {
      val m = spy(new Delete(rowkey))
      HDelete.withDurability(Durability.ASYNC_WAL).run(m)
      verify(m).setDurability(Durability.ASYNC_WAL)
    }
  }
  describe("withId") {
    it("should call setId") {
      val m = spy(new Delete(rowkey))
      HDelete.withId("1").run(m)
      verify(m).setId("1")
    }
  }
  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new Delete(rowkey))
      HDelete.withAttribute("n", Bytes.toBytes("v")).run(m)
      verify(m).setAttribute("n", Bytes.toBytes("v"))
    }
  }
  describe("withFamily") {
    it("should call addFamily") {
      val m  = spy(new Delete(rowkey))
      val cf = Bytes.toBytes("a")
      HDelete.withFamily(cf).run(m)
      verify(m).addFamily(cf)
    }
  }
  describe("withFamilyTo") {
    it("should call addFamily") {
      val m  = spy(new Delete(rowkey))
      val cf = Bytes.toBytes("a")
      HDelete.withFamilyTo(cf, 10).run(m)
      verify(m).addFamily(cf, 10)
    }
  }
  describe("withFamilyVersion") {
    it("should call addFamily") {
      val m  = spy(new Delete(rowkey))
      val cf = Bytes.toBytes("a")
      HDelete.withFamilyVersion(cf, 10).run(m)
      verify(m).addFamilyVersion(cf, 10)
    }
  }
  describe("withColumnLatest") {
    it("should call addColumn") {
      val m  = spy(new Delete(rowkey))
      val cf = Bytes.toBytes("a")
      HDelete.withColumnLatest(cf, 10).run(m)
      verify(m).addColumn(cf, Bytes.toBytes(10))
    }
  }
  describe("withColumnVersion") {
    it("should call addColumn") {
      val m  = spy(new Delete(rowkey))
      val cf = Bytes.toBytes("a")
      HDelete.withColumnVersion(cf, 10, 10).run(m)
      verify(m).addColumn(cf, Bytes.toBytes(10), 10)
    }
  }
  describe("withColumns") {
    it("should call addColumns") {
      val m  = spy(new Delete(rowkey))
      val cf = Bytes.toBytes("a")
      HDelete.withColumns(cf, 10).run(m)
      verify(m).addColumns(cf, Bytes.toBytes(10))
    }
  }
  describe("withColumnsVersion") {
    it("should call addColumns") {
      val m  = spy(new Delete(rowkey))
      val cf = Bytes.toBytes("a")
      HDelete.withColumnsVersion(cf, 10, 10).run(m)
      verify(m).addColumns(cf, Bytes.toBytes(10), 10)
    }
  }
}
