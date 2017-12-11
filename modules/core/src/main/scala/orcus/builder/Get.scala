package orcus.builder

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Get => HGet}
import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations

final class Get(_obj: HGet) {

  def withACL(user: String, perms: Permission): Get = {
    _obj.setACL(user, perms)
    this
  }

  def withAuthorizations(authorizations: Authorizations): Get = {
    _obj.setAuthorizations(authorizations)
    this
  }

  def withId(id: String): Get = {
    _obj.setId(id)
    this
  }

  def withAttribute(name: String, value: Array[Byte]): Get = {
    _obj.setAttribute(name, value)
    this
  }

  def withFamily(family: Array[Byte]): Get = {
    _obj.addFamily(family)
    this
  }

  def withColumn[K](family: Array[Byte], qualifier: K)(implicit
                                                       K: ValueCodec[K]): Get = {
    _obj.addColumn(family, K.encode(qualifier))
    this
  }

  def withCacheBlocks(cacheBlocks: Boolean): Get = {
    _obj.setCacheBlocks(cacheBlocks)
    this
  }

  def withCheckExistenceOnly(checkExistenceOnly: Boolean): Get = {
    _obj.setCheckExistenceOnly(checkExistenceOnly)
    this
  }

  def withClosestRowBefore(closestRowBefore: Boolean): Get = {
    _obj.setClosestRowBefore(closestRowBefore)
    this
  }

  def withConsistency(consistency: Consistency): Get = {
    _obj.setConsistency(consistency)
    this
  }

  def withFilter(filter: Filter): Get = {
    _obj.setFilter(filter)
    this
  }

  def withIsolationLevel(level: IsolationLevel): Get = {
    _obj.setIsolationLevel(level)
    this
  }

  def withMaxResultsPerColumnFamily(limit: Int): Get = {
    _obj.setMaxResultsPerColumnFamily(limit)
    this
  }

  def withMaxVersions(max: Int = Int.MaxValue): Get = {
    _obj.setMaxVersions(max)
    this
  }

  def withReplicaId(id: Int): Get = {
    _obj.setReplicaId(id)
    this
  }

  def withRowOffsetPerColumnFamily(offset: Int): Get = {
    _obj.setRowOffsetPerColumnFamily(offset)
    this
  }

  def withTimeRange(min: Long, max: Long): Get = {
    _obj.setTimeRange(min, max)
    this
  }

  def withTimeStamp(timestamp: Long): Get = {
    _obj.setTimeStamp(timestamp)
    this
  }

  def get: HGet = _obj
}
