package orcus.builder

import cats.data.Reader
import orcus.codec.{empty, ValueCodec}
import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Get}
import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations

object HGet {

  def withACL(user: String, perms: Permission): Reader[Get, Get] =
    Reader(_.setACL(user, perms))

  def withAuthorizations(authorizations: Authorizations): Reader[Get, Get] =
    Reader(_.setAuthorizations(authorizations))

  def withId(id: String): Reader[Get, Get] =
    Reader(_.setId(id))

  def withAttribute(name: String, value: Array[Byte]): Reader[Get, Get] =
    Reader(_.setAttribute(name, value))

  def withFamily(family: Array[Byte]): Reader[Get, Get] =
    Reader(_.addFamily(family))

  def withColumn[K](family: Array[Byte], qualifier: K)(implicit
                                                       K: ValueCodec[K]): Reader[Get, Get] =
    Reader(_.addColumn(family, K.encode(Option(qualifier)).getOrElse(empty)))

  def withCacheBlocks(cacheBlocks: Boolean): Reader[Get, Get] =
    Reader(_.setCacheBlocks(cacheBlocks))

  def withCheckExistenceOnly(checkExistenceOnly: Boolean): Reader[Get, Get] =
    Reader(_.setCheckExistenceOnly(checkExistenceOnly))

  def withClosestRowBefore(closestRowBefore: Boolean): Reader[Get, Get] =
    Reader(_.setClosestRowBefore(closestRowBefore))

  def withConsistency(consistency: Consistency): Reader[Get, Get] =
    Reader(_.setConsistency(consistency))

  def withFilter(filter: Filter): Reader[Get, Get] =
    Reader(_.setFilter(filter))

  def withIsolationLevel(level: IsolationLevel): Reader[Get, Get] =
    Reader(_.setIsolationLevel(level))

  def withMaxResultsPerColumnFamily(limit: Int): Reader[Get, Get] =
    Reader(_.setMaxResultsPerColumnFamily(limit))

  def withMaxVersions(max: Int = Int.MaxValue): Reader[Get, Get] =
    Reader(_.setMaxVersions(max))

  def withReplicaId(id: Int): Reader[Get, Get] =
    Reader(_.setReplicaId(id))

  def withRowOffsetPerColumnFamily(offset: Int): Reader[Get, Get] =
    Reader(_.setRowOffsetPerColumnFamily(offset))

  def withTimeRange(min: Long, max: Long): Reader[Get, Get] =
    Reader(_.setTimeRange(min, max))

  def withTimeStamp(timestamp: Long): Reader[Get, Get] =
    Reader(_.setTimeStamp(timestamp))
}
