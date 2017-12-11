package orcus.builder

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Scan => HScan}
import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations

final class Scan(_obj: HScan)(implicit keyCodec: ValueCodec[String]) {

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setACL]]
    */
  def withACL(user: String, perms: Permission): Scan = {
    _obj.setACL(user, perms)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setAuthorizations]]
    */
  def withAuthorizations(authorizations: Authorizations): Scan = {
    _obj.setAuthorizations(authorizations)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setId]]
    */
  def withId(id: String): Scan = {
    _obj.setId(id)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setAttribute]]
    */
  def withAttribute(name: String, value: Array[Byte]): Scan = {
    _obj.setAttribute(name, value)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#addFamily]]
    */
  def withFamily(family: String): Scan = {
    _obj.addFamily(keyCodec.encode(family))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#addColumn]]
    */
  def withColumn[K](family: String, qualifier: K)(implicit
                                                  K: ValueCodec[K]): Scan = {
    _obj.addColumn(keyCodec.encode(family), K.encode(qualifier))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setCacheBlocks]]
    */
  def withCacheBlocks(cacheBlocks: Boolean): Scan = {
    _obj.setCacheBlocks(cacheBlocks)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setAllowPartialResults]]
    */
  def withAllowPartialResults(allowPartialResults: Boolean): Scan = {
    _obj.setAllowPartialResults(allowPartialResults)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setCaching]]
    */
  def withCaching(size: Int): Scan = {
    _obj.setCaching(size)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setSmall]]
    */
  def withSmall(small: Boolean): Scan = {
    _obj.setSmall(small)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setMaxResultSize]]
    */
  def withMaxResultSize(size: Long): Scan = {
    _obj.setMaxResultSize(size)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setStartRow]]
    */
  def withStartRow(row: String): Scan = {
    _obj.setStartRow(keyCodec.encode(row))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setStopRow]]
    */
  def withStopRow(row: String): Scan = {
    _obj.setStopRow(keyCodec.encode(row))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setScanMetricsEnabled]]
    */
  def withScanMetricsEnabled(enabled: Boolean): Scan = {
    _obj.setScanMetricsEnabled(enabled)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setLoadColumnFamiliesOnDemand]]
    */
  def withLoadColumnFamiliesOnDemand(value: Boolean): Scan = {
    _obj.setLoadColumnFamiliesOnDemand(value)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setColumnFamilyTimeRange]]
    */
  def withColumnFamilyTimeRange(family: String, min: Long, max: Long): Scan = {
    _obj.setColumnFamilyTimeRange(keyCodec.encode(family), min, max)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setBatch]]
    */
  def withBatch(size: Int): Scan = {
    _obj.setBatch(size)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setConsistency]]
    */
  def withConsistency(consistency: Consistency): Scan = {
    _obj.setConsistency(consistency)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setFilter]]
    */
  def withFilter(filter: Filter): Scan = {
    _obj.setFilter(filter)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setIsolationLevel]]
    */
  def withIsolationLevel(level: IsolationLevel): Scan = {
    _obj.setIsolationLevel(level)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setMaxResultsPerColumnFamily]]
    */
  def withMaxResultsPerColumnFamily(limit: Int): Scan = {
    _obj.setMaxResultsPerColumnFamily(limit)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setMaxVersions]]
    */
  def withMaxVersions(max: Int = _obj.getMaxVersions): Scan = {
    _obj.setMaxVersions(max)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setReplicaId]]
    */
  def withReplicaId(id: Int): Scan = {
    _obj.setReplicaId(id)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setRowOffsetPerColumnFamily]]
    */
  def withRowOffsetPerColumnFamily(offset: Int): Scan = {
    _obj.setRowOffsetPerColumnFamily(offset)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setTimeRange]]
    */
  def withTimeRange(min: Long, max: Long): Scan = {
    _obj.setTimeRange(min, max)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Scan#setTimeStamp]]
    */
  def withTimeStamp(timestamp: Long): Scan = {
    _obj.setTimeStamp(timestamp)
    this
  }

  /**
    * Returns [[org.apache.hadoop.hbase.client.Scan]]
    */
  def get: HScan = _obj
}
