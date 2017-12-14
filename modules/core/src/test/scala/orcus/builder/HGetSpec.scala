package orcus.builder

import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Get}
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

class HGetSpec extends BuilderSpec {

  describe("withACL") {
    it("should call setACL") {
      val m = spy(new Get(rowkey))
      val v = new Permission
      HGet.withACL("1", v).run(m)
      verify(m).setACL("1", v)
    }
  }
  describe("withAuthorizations") {
    it("should call setAuthorizations") {
      val m = spy(new Get(rowkey))
      val v = new Authorizations("a")
      HGet.withAuthorizations(v).run(m)
      verify(m).setAuthorizations(v)
    }
  }
  describe("withId") {
    it("should call setId") {
      val m = spy(new Get(rowkey))
      HGet.withId("1").run(m)
      verify(m).setId("1")
    }
  }
  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new Get(rowkey))
      HGet.withAttribute("n", Bytes.toBytes("v")).run(m)
      verify(m).setAttribute("n", Bytes.toBytes("v"))
    }
  }
  describe("withFamily") {
    it("should call addFamily") {
      val m  = spy(new Get(rowkey))
      val cf = Bytes.toBytes("a")
      HGet.withFamily(cf).run(m)
      verify(m).addFamily(cf)
    }
  }
  describe("withColumn") {
    it("should call addColumn") {
      val m  = spy(new Get(rowkey))
      val cf = Bytes.toBytes("a")
      HGet.withColumn(cf, "b").run(m)
      verify(m).addColumn(cf, Bytes.toBytes("b"))
    }
  }
  describe("withCacheBlocks") {
    it("should call setCacheBlocks") {
      val m = spy(new Get(rowkey))
      HGet.withCacheBlocks(true).run(m)
      verify(m).setCacheBlocks(true)
    }
  }
  describe("withCheckExistenceOnly") {
    it("should call setCheckExistenceOnly") {
      val m = spy(new Get(rowkey))
      HGet.withCheckExistenceOnly(true).run(m)
      verify(m).setCheckExistenceOnly(true)
    }
  }
  describe("withClosestRowBefore") {
    it("should call setClosestRowBefore") {
      val m = spy(new Get(rowkey))
      HGet.withClosestRowBefore(true).run(m)
      verify(m).setClosestRowBefore(true)
    }
  }
  describe("withConsistency") {
    it("should call setConsistency") {
      val m = spy(new Get(rowkey))
      HGet.withConsistency(Consistency.TIMELINE).run(m)
      verify(m).setConsistency(Consistency.TIMELINE)
    }
  }
  describe("withFilter") {
    it("should call setFilter") {
      val m = spy(new Get(rowkey))
      val v = new ColumnPrefixFilter(Bytes.toBytes("s"))
      HGet.withFilter(v).run(m)
      verify(m).setFilter(v)
    }
  }
  describe("withIsolationLevel") {
    it("should call setIsolationLevel") {
      val m = spy(new Get(rowkey))
      HGet.withIsolationLevel(IsolationLevel.READ_UNCOMMITTED).run(m)
      verify(m).setIsolationLevel(IsolationLevel.READ_UNCOMMITTED)
    }
  }
  describe("withMaxResultsPerColumnFamily") {
    it("should call setMaxResultsPerColumnFamily") {
      val m = spy(new Get(rowkey))
      HGet.withMaxResultsPerColumnFamily(100).run(m)
      verify(m).setMaxResultsPerColumnFamily(100)
    }
  }
  describe("withMaxVersions") {
    it("should call withMaxVersions") {
      val m = spy(new Get(rowkey))
      HGet.withMaxVersions(30).run(m)
      verify(m).setMaxVersions(30)
    }
  }
  describe("withReplicaId") {
    it("should call setReplicaId") {
      val m = spy(new Get(rowkey))
      HGet.withReplicaId(10).run(m)
      verify(m).setReplicaId(10)
    }
  }
  describe("withRowOffsetPerColumnFamily") {
    it("should call setRowOffsetPerColumnFamily") {
      val m = spy(new Get(rowkey))
      HGet.withRowOffsetPerColumnFamily(1).run(m)
      verify(m).setRowOffsetPerColumnFamily(1)
    }
  }
  describe("withTimeRange") {
    it("should call setTimeRange") {
      val m = spy(new Get(rowkey))
      HGet.withTimeRange(1, 2).run(m)
      verify(m).setTimeRange(1, 2)
    }
  }
  describe("withTimeStamp") {
    it("should call setTimeStamp") {
      val m = spy(new Get(rowkey))
      HGet.withTimeStamp(1).run(m)
      verify(m).setTimeStamp(1)
    }
  }
}
