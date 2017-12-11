package orcus.builder

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Get => HGet}
import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations

final class Get(_obj: HGet)(implicit keyCodec: ValueCodec[String]) {

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setACL]]
    */
  def withACL(user: String, perms: Permission): Get = {
    _obj.setACL(user, perms)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setAuthorizations]]
    */
  def withAuthorizations(authorizations: Authorizations): Get = {
    _obj.setAuthorizations(authorizations)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setId]]
    */
  def withId(id: String): Get = {
    _obj.setId(id)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setAttribute]]
    */
  def withAttribute(name: String, value: Array[Byte]): Get = {
    _obj.setAttribute(name, value)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#addFamily(byte[])]]
    */
  def withFamily(family: String): Get = {
    _obj.addFamily(keyCodec.encode(family))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#addColumn(byte[], byte[])]]
    */
  def withColumn[K](family: String, qualifier: K)(implicit
                                                  K: ValueCodec[K]): Get = {
    _obj.addColumn(keyCodec.encode(family), K.encode(qualifier))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setCacheBlocks]]
    */
  def withCacheBlocks(cacheBlocks: Boolean): Get = {
    _obj.setCacheBlocks(cacheBlocks)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setCheckExistenceOnly]]
    */
  def withCheckExistenceOnly(checkExistenceOnly: Boolean): Get = {
    _obj.setCheckExistenceOnly(checkExistenceOnly)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setClosestRowBefore]]
    */
  def withClosestRowBefore(closestRowBefore: Boolean): Get = {
    _obj.setClosestRowBefore(closestRowBefore)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setConsistency]]
    */
  def withConsistency(consistency: Consistency): Get = {
    _obj.setConsistency(consistency)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setFilter]]
    */
  def withFilter(filter: Filter): Get = {
    _obj.setFilter(filter)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setIsolationLevel]]
    */
  def withIsolationLevel(level: IsolationLevel): Get = {
    _obj.setIsolationLevel(level)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get]]
    */
  def withMaxResultsPerColumnFamily(limit: Int): Get = {
    _obj.setMaxResultsPerColumnFamily(limit)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setMaxVersions(int)]]
    */
  def withMaxVersions(max: Int = Int.MaxValue): Get = {
    _obj.setMaxVersions(max)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setReplicaId]]
    */
  def withReplicaId(id: Int): Get = {
    _obj.setReplicaId(id)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setRowOffsetPerColumnFamily]]
    */
  def withRowOffsetPerColumnFamily(offset: Int): Get = {
    _obj.setRowOffsetPerColumnFamily(offset)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setTimeRange]]
    */
  def withTimeRange(min: Long, max: Long): Get = {
    _obj.setTimeRange(min, max)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Get#setTimeStamp]]
    */
  def withTimeStamp(timestamp: Long): Get = {
    _obj.setTimeStamp(timestamp)
    this
  }

  /**
    * Returns [[org.apache.hadoop.hbase.client.Get]]
    */
  def get: HGet = _obj
}
