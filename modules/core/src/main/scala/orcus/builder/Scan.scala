package orcus.builder

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Scan => HScan}
import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations

final class Scan(_obj: HScan) {

  def withACL(user: String, perms: Permission): Scan = {
    _obj.setACL(user, perms)
    this
  }

  def withAuthorizations(authorizations: Authorizations): Scan = {
    _obj.setAuthorizations(authorizations)
    this
  }

  def withId(id: String): Scan = {
    _obj.setId(id)
    this
  }

  def withAttribute(name: String, value: Array[Byte]): Scan = {
    _obj.setAttribute(name, value)
    this
  }

  def withRowPrefixFilter(prefix: Array[Byte]): Scan = {
    _obj.setRowPrefixFilter(prefix)
    this
  }

  def withFamily(family: Array[Byte]): Scan = {
    _obj.addFamily(family)
    this
  }

  def withColumn[K](family: Array[Byte], qualifier: K)(implicit
                                                       K: ValueCodec[K]): Scan = {
    _obj.addColumn(family, K.encode(qualifier))
    this
  }

  def withCacheBlocks(cacheBlocks: Boolean): Scan = {
    _obj.setCacheBlocks(cacheBlocks)
    this
  }

  def withAllowPartialResults(allowPartialResults: Boolean): Scan = {
    _obj.setAllowPartialResults(allowPartialResults)
    this
  }

  def withCaching(size: Int): Scan = {
    _obj.setCaching(size)
    this
  }

  def withSmall(small: Boolean): Scan = {
    _obj.setSmall(small)
    this
  }

  def withMaxResultSize(size: Long): Scan = {
    _obj.setMaxResultSize(size)
    this
  }

  def withStartRow(row: Array[Byte]): Scan = {
    _obj.setStartRow(row)
    this
  }

  def withStopRow(row: Array[Byte]): Scan = {
    _obj.setStopRow(row)
    this
  }

  def withScanMetricsEnabled(enabled: Boolean): Scan = {
    _obj.setScanMetricsEnabled(enabled)
    this
  }

  def withLoadColumnFamiliesOnDemand(value: Boolean): Scan = {
    _obj.setLoadColumnFamiliesOnDemand(value)
    this
  }

  def withColumnFamilyTimeRange(family: Array[Byte], min: Long, max: Long): Scan = {
    _obj.setColumnFamilyTimeRange(family, min, max)
    this
  }

  def withBatch(size: Int): Scan = {
    _obj.setBatch(size)
    this
  }

  def withConsistency(consistency: Consistency): Scan = {
    _obj.setConsistency(consistency)
    this
  }

  def withFilter(filter: Filter): Scan = {
    _obj.setFilter(filter)
    this
  }

  def withIsolationLevel(level: IsolationLevel): Scan = {
    _obj.setIsolationLevel(level)
    this
  }

  def withMaxResultsPerColumnFamily(limit: Int): Scan = {
    _obj.setMaxResultsPerColumnFamily(limit)
    this
  }

  def withMaxVersions(max: Int = _obj.getMaxVersions): Scan = {
    _obj.setMaxVersions(max)
    this
  }

  def withReplicaId(id: Int): Scan = {
    _obj.setReplicaId(id)
    this
  }

  def withRowOffsetPerColumnFamily(offset: Int): Scan = {
    _obj.setRowOffsetPerColumnFamily(offset)
    this
  }

  def withTimeRange(min: Long, max: Long): Scan = {
    _obj.setTimeRange(min, max)
    this
  }

  def withTimeStamp(timestamp: Long): Scan = {
    _obj.setTimeStamp(timestamp)
    this
  }

  def get: HScan = _obj
}
