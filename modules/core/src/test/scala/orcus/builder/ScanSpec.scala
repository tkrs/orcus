package orcus.builder

import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Scan => HScan}
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.Mockito._

class ScanSpec extends BuilderSpec {

  describe("withACL") {
    it("should call setACL") {
      val m = spy(new HScan(rowkey))
      val v = new Permission
      new Scan(m).withACL("1", v)
      verify(m).setACL("1", v)
    }
  }
  describe("withAuthorizations") {
    it("should call setAuthorizations") {
      val m = spy(new HScan(rowkey))
      val v = new Authorizations("a")
      new Scan(m).withAuthorizations(v)
      verify(m).setAuthorizations(v)
    }
  }
  describe("withId") {
    it("should call setId") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withId("1")
      verify(m).setId("1")
    }
  }
  describe("withAttribute") {
    it("should call setAttribute") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withAttribute("n", Bytes.toBytes("v"))
      verify(m).setAttribute("n", Bytes.toBytes("v"))
    }
  }
  describe("withRowPrefixFilter") {
    it("should call setRowPrefixFilter") {
      val m      = spy(new HScan())
      val prefix = Bytes.toBytes("abc")
      new Scan(m).withRowPrefixFilter(prefix)
      verify(m).setRowPrefixFilter(prefix)
    }
  }
  describe("withFamily") {
    it("should call addFamily") {
      val m  = spy(new HScan(rowkey))
      val cf = Bytes.toBytes("a")
      new Scan(m).withFamily(cf)
      verify(m).addFamily(cf)
    }
  }
  describe("withColumn") {
    it("should call addColumn") {
      val m  = spy(new HScan(rowkey))
      val cf = Bytes.toBytes("a")
      new Scan(m).withColumn(cf, "b")
      verify(m).addColumn(cf, Bytes.toBytes("b"))
    }
  }
  describe("withCacheBlocks") {
    it("should call setCacheBlocks") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withCacheBlocks(true)
      verify(m).setCacheBlocks(true)
    }
  }
  describe("withAllowPartialResults") {
    it("should call setAllowPartialResults") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withAllowPartialResults(true)
      verify(m).setAllowPartialResults(true)
    }
  }
  describe("withCaching") {
    it("should call setCaching") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withCaching(100)
      verify(m).setCaching(100)
    }
  }
  describe("withSmall") {
    it("should call setSmall") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withSmall(true)
      verify(m).setSmall(true)
    }
  }
  describe("withMaxResultSize") {
    it("should call setMaxResultSize") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withMaxResultSize(10)
      verify(m).setMaxResultSize(10)
    }
  }
  describe("withStartRow") {
    it("should call setStartRow") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withStartRow(rowkey)
      verify(m).setStartRow(rowkey)
    }
  }
  describe("withStopRow") {
    it("should call setStopRow") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withStopRow(rowkey)
      verify(m).setStopRow(rowkey)
    }
  }
  describe("withScanMetricsEnabled") {
    it("should call setScanMetricsEnabled") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withScanMetricsEnabled(true)
      verify(m).setScanMetricsEnabled(true)
    }
  }
  describe("withLoadColumnFamiliesOnDemand") {
    it("should call setLoadColumnFamiliesOnDemand") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withLoadColumnFamiliesOnDemand(true)
      verify(m).setLoadColumnFamiliesOnDemand(true)
    }
  }
  describe("withColumnFamilyTimeRange") {
    it("should call setColumnFamilyTimeRange") {
      val m  = spy(new HScan(rowkey))
      val cf = Bytes.toBytes("a")
      new Scan(m).withColumnFamilyTimeRange(cf, 1, 2)
      verify(m).setColumnFamilyTimeRange(cf, 1, 2)
    }
  }
  describe("withBatch") {
    it("should call setBatch") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withBatch(1)
      verify(m).setBatch(1)
    }
  }
  describe("withConsistency") {
    it("should call setConsistency") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withConsistency(Consistency.TIMELINE)
      verify(m).setConsistency(Consistency.TIMELINE)
    }
  }
  describe("withFilter") {
    it("should call setFilter") {
      val m = spy(new HScan(rowkey))
      val v = new ColumnPrefixFilter(Bytes.toBytes("s"))
      new Scan(m).withFilter(v)
      verify(m).setFilter(v)
    }
  }
  describe("withIsolationLevel") {
    it("should call setIsolationLevel") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withIsolationLevel(IsolationLevel.READ_UNCOMMITTED)
      verify(m).setIsolationLevel(IsolationLevel.READ_UNCOMMITTED)
    }
  }
  describe("withMaxResultsPerColumnFamily") {
    it("should call setMaxResultsPerColumnFamily") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withMaxResultsPerColumnFamily(100)
      verify(m).setMaxResultsPerColumnFamily(100)
    }
  }
  describe("withMaxVersions") {
    it("should call setMaxVersions") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withMaxVersions(30)
      verify(m).setMaxVersions(30)
    }
  }
  describe("withReplicaId") {
    it("should call setReplicaId") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withReplicaId(10)
      verify(m).setReplicaId(10)
    }
  }
  describe("withRowOffsetPerColumnFamily") {
    it("should call setRowOffsetPerColumnFamily") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withRowOffsetPerColumnFamily(1)
      verify(m).setRowOffsetPerColumnFamily(1)
    }
  }
  describe("withTimeRange") {
    it("should call setTimeRange") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withTimeRange(1, 2)
      verify(m).setTimeRange(1, 2)
    }
  }
  describe("withTimeStamp") {
    it("should call setTimeStamp") {
      val m = spy(new HScan(rowkey))
      new Scan(m).withTimeStamp(1)
      verify(m).setTimeStamp(1)
    }
  }
}
