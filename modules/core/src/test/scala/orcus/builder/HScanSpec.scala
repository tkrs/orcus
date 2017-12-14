package orcus.builder

import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Scan}
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

class HScanSpec extends BuilderSpec {

  describe("withACL") {
    it("should call setACL") {
      val m = spy(new Scan(rowkey))
      val v = new Permission
      HScan.withACL("1", v).run(m)
      verify(m).setACL("1", v)
    }
  }
  describe("withAuthorizations") {
    it("should call setAuthorizations") {
      val m = spy(new Scan(rowkey))
      val v = new Authorizations("a")
      HScan.withAuthorizations(v).run(m)
      verify(m).setAuthorizations(v)
    }
  }
  describe("withId") {
    it("should call setId") {
      val m = spy(new Scan(rowkey))
      HScan.withId("1").run(m)
      verify(m).setId("1")
    }
  }
  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new Scan(rowkey))
      HScan.withAttribute("n", Bytes.toBytes("v")).run(m)
      verify(m).setAttribute("n", Bytes.toBytes("v"))
    }
  }
  describe("withRowPrefixFilter") {
    it("should call setRowPrefixFilter") {
      val m      = spy(new Scan())
      val prefix = Bytes.toBytes("abc")
      HScan.withRowPrefixFilter(prefix).run(m)
      verify(m).setRowPrefixFilter(prefix)
    }
  }
  describe("withFamily") {
    it("should call addFamily") {
      val m  = spy(new Scan(rowkey))
      val cf = Bytes.toBytes("a")
      HScan.withFamily(cf).run(m)
      verify(m).addFamily(cf)
    }
  }
  describe("withColumn") {
    it("should call addColumn") {
      val m  = spy(new Scan(rowkey))
      val cf = Bytes.toBytes("a")
      HScan.withColumn(cf, "b").run(m)
      verify(m).addColumn(cf, Bytes.toBytes("b"))
    }
  }
  describe("withCacheBlocks") {
    it("should call setCacheBlocks") {
      val m = spy(new Scan(rowkey))
      HScan.withCacheBlocks(true).run(m)
      verify(m).setCacheBlocks(true)
    }
  }
  describe("withAllowPartialResults") {
    it("should call setAllowPartialResults") {
      val m = spy(new Scan(rowkey))
      HScan.withAllowPartialResults(true).run(m)
      verify(m).setAllowPartialResults(true)
    }
  }
  describe("withCaching") {
    it("should call setCaching") {
      val m = spy(new Scan(rowkey))
      HScan.withCaching(100).run(m)
      verify(m).setCaching(100)
    }
  }
  describe("withSmall") {
    it("should call setSmall") {
      val m = spy(new Scan(rowkey))
      HScan.withSmall(true).run(m)
      verify(m).setSmall(true)
    }
  }
  describe("withMaxResultSize") {
    it("should call setMaxResultSize") {
      val m = spy(new Scan(rowkey))
      HScan.withMaxResultSize(10).run(m)
      verify(m).setMaxResultSize(10)
    }
  }
  describe("withStartRow") {
    it("should call setStartRow") {
      val m = spy(new Scan(rowkey))
      HScan.withStartRow(rowkey).run(m)
      verify(m).setStartRow(rowkey)
    }
  }
  describe("withStopRow") {
    it("should call setStopRow") {
      val m = spy(new Scan(rowkey))
      HScan.withStopRow(rowkey).run(m)
      verify(m).setStopRow(rowkey)
    }
  }
  describe("withScanMetricsEnabled") {
    it("should call setScanMetricsEnabled") {
      val m = spy(new Scan(rowkey))
      HScan.withScanMetricsEnabled(true).run(m)
      verify(m).setScanMetricsEnabled(true)
    }
  }
  describe("withLoadColumnFamiliesOnDemand") {
    it("should call setLoadColumnFamiliesOnDemand") {
      val m = spy(new Scan(rowkey))
      HScan.withLoadColumnFamiliesOnDemand(true).run(m)
      verify(m).setLoadColumnFamiliesOnDemand(true)
    }
  }
  describe("withColumnFamilyTimeRange") {
    it("should call setColumnFamilyTimeRange") {
      val m  = spy(new Scan(rowkey))
      val cf = Bytes.toBytes("a")
      HScan.withColumnFamilyTimeRange(cf, 1, 2).run(m)
      verify(m).setColumnFamilyTimeRange(cf, 1, 2)
    }
  }
  describe("withBatch") {
    it("should call setBatch") {
      val m = spy(new Scan(rowkey))
      HScan.withBatch(1).run(m)
      verify(m).setBatch(1)
    }
  }
  describe("withConsistency") {
    it("should call setConsistency") {
      val m = spy(new Scan(rowkey))
      HScan.withConsistency(Consistency.TIMELINE).run(m)
      verify(m).setConsistency(Consistency.TIMELINE)
    }
  }
  describe("withFilter") {
    it("should call setFilter") {
      val m = spy(new Scan(rowkey))
      val v = new ColumnPrefixFilter(Bytes.toBytes("s"))
      HScan.withFilter(v).run(m)
      verify(m).setFilter(v)
    }
  }
  describe("withIsolationLevel") {
    it("should call setIsolationLevel") {
      val m = spy(new Scan(rowkey))
      HScan.withIsolationLevel(IsolationLevel.READ_UNCOMMITTED).run(m)
      verify(m).setIsolationLevel(IsolationLevel.READ_UNCOMMITTED)
    }
  }
  describe("withMaxResultsPerColumnFamily") {
    it("should call setMaxResultsPerColumnFamily") {
      val m = spy(new Scan(rowkey))
      HScan.withMaxResultsPerColumnFamily(100).run(m)
      verify(m).setMaxResultsPerColumnFamily(100)
    }
  }
  describe("withMaxVersions") {
    it("should call setMaxVersions") {
      val m = spy(new Scan(rowkey))
      HScan.withMaxVersions(30).run(m)
      verify(m).setMaxVersions(30)
    }
  }
  describe("withReplicaId") {
    it("should call setReplicaId") {
      val m = spy(new Scan(rowkey))
      HScan.withReplicaId(10).run(m)
      verify(m).setReplicaId(10)
    }
  }
  describe("withRowOffsetPerColumnFamily") {
    it("should call setRowOffsetPerColumnFamily") {
      val m = spy(new Scan(rowkey))
      HScan.withRowOffsetPerColumnFamily(1).run(m)
      verify(m).setRowOffsetPerColumnFamily(1)
    }
  }
  describe("withTimeRange") {
    it("should call setTimeRange") {
      val m = spy(new Scan(rowkey))
      HScan.withTimeRange(1, 2).run(m)
      verify(m).setTimeRange(1, 2)
    }
  }
  describe("withTimeStamp") {
    it("should call setTimeStamp") {
      val m = spy(new Scan(rowkey))
      HScan.withTimeStamp(1).run(m)
      verify(m).setTimeStamp(1)
    }
  }
}
