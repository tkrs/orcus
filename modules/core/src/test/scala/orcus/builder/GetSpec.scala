package orcus.builder

import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Get => HGet}
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

class GetSpec extends BuilderSpec {

  describe("withACL") {
    it("should call setACL") {
      val m = spy(new HGet(rowkey))
      val v = new Permission
      new Get(m).withACL("1", v)
      verify(m).setACL("1", v)
    }
  }
  describe("withAuthorizations") {
    it("should call setAuthorizations") {
      val m = spy(new HGet(rowkey))
      val v = new Authorizations("a")
      new Get(m).withAuthorizations(v)
      verify(m).setAuthorizations(v)
    }
  }
  describe("withId") {
    it("should call setId") {
      val m = spy(new HGet(rowkey))
      new Get(m).withId("1")
      verify(m).setId("1")
    }
  }
  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new HGet(rowkey))
      new Get(m).withAttribute("n", Bytes.toBytes("v"))
      verify(m).setAttribute("n", Bytes.toBytes("v"))
    }
  }
  describe("withFamily") {
    it("should call addFamily") {
      val m = spy(new HGet(rowkey))
      new Get(m).withFamily("a")
      verify(m).addFamily(Bytes.toBytes("a"))
    }
  }
  describe("withColumn") {
    it("should call addColumn") {
      val m = spy(new HGet(rowkey))
      new Get(m).withColumn("a", "b")
      verify(m).addColumn(Bytes.toBytes("a"), Bytes.toBytes("b"))
    }
  }
  describe("withCacheBlocks") {
    it("should call setCacheBlocks") {
      val m = spy(new HGet(rowkey))
      new Get(m).withCacheBlocks(true)
      verify(m).setCacheBlocks(true)
    }
  }
  describe("withCheckExistenceOnly") {
    it("should call setCheckExistenceOnly") {
      val m = spy(new HGet(rowkey))
      new Get(m).withCheckExistenceOnly(true)
      verify(m).setCheckExistenceOnly(true)
    }
  }
  describe("withClosestRowBefore") {
    it("should call setClosestRowBefore") {
      val m = spy(new HGet(rowkey))
      new Get(m).withClosestRowBefore(true)
      verify(m).setClosestRowBefore(true)
    }
  }
  describe("withConsistency") {
    it("should call setConsistency") {
      val m = spy(new HGet(rowkey))
      new Get(m).withConsistency(Consistency.TIMELINE)
      verify(m).setConsistency(Consistency.TIMELINE)
    }
  }
  describe("withFilter") {
    it("should call setFilter") {
      val m = spy(new HGet(rowkey))
      val v = new ColumnPrefixFilter(Bytes.toBytes("s"))
      new Get(m).withFilter(v)
      verify(m).setFilter(v)
    }
  }
  describe("withIsolationLevel") {
    it("should call setIsolationLevel") {
      val m = spy(new HGet(rowkey))
      new Get(m).withIsolationLevel(IsolationLevel.READ_UNCOMMITTED)
      verify(m).setIsolationLevel(IsolationLevel.READ_UNCOMMITTED)
    }
  }
  describe("withMaxResultsPerColumnFamily") {
    it("should call setMaxResultsPerColumnFamily") {
      val m = spy(new HGet(rowkey))
      new Get(m).withMaxResultsPerColumnFamily(100)
      verify(m).setMaxResultsPerColumnFamily(100)
    }
  }
  describe("withMaxVersions") {
    it("should call withMaxVersions") {
      val m = spy(new HGet(rowkey))
      new Get(m).withMaxVersions(30)
      verify(m).setMaxVersions(30)
    }
  }
  describe("withReplicaId") {
    it("should call setReplicaId") {
      val m = spy(new HGet(rowkey))
      new Get(m).withReplicaId(10)
      verify(m).setReplicaId(10)
    }
  }
  describe("withRowOffsetPerColumnFamily") {
    it("should call setRowOffsetPerColumnFamily") {
      val m = spy(new HGet(rowkey))
      new Get(m).withRowOffsetPerColumnFamily(1)
      verify(m).setRowOffsetPerColumnFamily(1)
    }
  }
  describe("withTimeRange") {
    it("should call setTimeRange") {
      val m = spy(new HGet(rowkey))
      new Get(m).withTimeRange(1, 2)
      verify(m).setTimeRange(1, 2)
    }
  }
  describe("withTimeStamp") {
    it("should call setTimeStamp") {
      val m = spy(new HGet(rowkey))
      new Get(m).withTimeStamp(1)
      verify(m).setTimeStamp(1)
    }
  }
}
