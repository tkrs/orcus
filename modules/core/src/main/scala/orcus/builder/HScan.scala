package orcus.builder

import cats.data.Reader
import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Consistency, IsolationLevel, Scan}
import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.Authorizations

object HScan {

  def withACL(user: String, perms: Permission): Reader[Scan, Scan] =
    Reader(_.setACL(user, perms))

  def withAuthorizations(authorizations: Authorizations): Reader[Scan, Scan] =
    Reader(_.setAuthorizations(authorizations))

  def withId(id: String): Reader[Scan, Scan] =
    Reader(_.setId(id))

  def withAttribute(name: String, value: Array[Byte]): Reader[Scan, Scan] =
    Reader(_.setAttribute(name, value))

  def withRowPrefixFilter(prefix: Array[Byte]): Reader[Scan, Scan] =
    Reader(_.setRowPrefixFilter(prefix))

  def withFamily(family: Array[Byte]): Reader[Scan, Scan] =
    Reader(_.addFamily(family))

  def withColumn[K](family: Array[Byte], qualifier: K)(implicit
                                                       K: ValueCodec[K]): Reader[Scan, Scan] =
    Reader(_.addColumn(family, K.encode(qualifier)))

  def withCacheBlocks(cacheBlocks: Boolean): Reader[Scan, Scan] =
    Reader(_.setCacheBlocks(cacheBlocks))

  def withAllowPartialResults(allowPartialResults: Boolean): Reader[Scan, Scan] =
    Reader(_.setAllowPartialResults(allowPartialResults))

  def withCaching(size: Int): Reader[Scan, Scan] =
    Reader(_.setCaching(size))

  def withSmall(small: Boolean): Reader[Scan, Scan] =
    Reader(_.setSmall(small))

  def withMaxResultSize(size: Long): Reader[Scan, Scan] =
    Reader(_.setMaxResultSize(size))

  def withStartRow(row: Array[Byte]): Reader[Scan, Scan] =
    Reader(_.setStartRow(row))

  def withStopRow(row: Array[Byte]): Reader[Scan, Scan] =
    Reader(_.setStopRow(row))

  def withScanMetricsEnabled(enabled: Boolean): Reader[Scan, Scan] =
    Reader(_.setScanMetricsEnabled(enabled))

  def withLoadColumnFamiliesOnDemand(value: Boolean): Reader[Scan, Scan] =
    Reader(_.setLoadColumnFamiliesOnDemand(value))

  def withColumnFamilyTimeRange(family: Array[Byte], min: Long, max: Long): Reader[Scan, Scan] =
    Reader(_.setColumnFamilyTimeRange(family, min, max))

  def withBatch(size: Int): Reader[Scan, Scan] =
    Reader(_.setBatch(size))

  def withConsistency(consistency: Consistency): Reader[Scan, Scan] =
    Reader(_.setConsistency(consistency))

  def withFilter(filter: Filter): Reader[Scan, Scan] =
    Reader(_.setFilter(filter))

  def withIsolationLevel(level: IsolationLevel): Reader[Scan, Scan] =
    Reader(_.setIsolationLevel(level))

  def withMaxResultsPerColumnFamily(limit: Int): Reader[Scan, Scan] =
    Reader(_.setMaxResultsPerColumnFamily(limit))

  def withMaxVersions(max: Int = Int.MaxValue): Reader[Scan, Scan] =
    Reader(_.setMaxVersions(max))

  def withReplicaId(id: Int): Reader[Scan, Scan] =
    Reader(_.setReplicaId(id))

  def withRowOffsetPerColumnFamily(offset: Int): Reader[Scan, Scan] =
    Reader(_.setRowOffsetPerColumnFamily(offset))

  def withTimeRange(min: Long, max: Long): Reader[Scan, Scan] =
    Reader(_.setTimeRange(min, max))

  def withTimeStamp(timestamp: Long): Reader[Scan, Scan] =
    Reader(_.setTimeStamp(timestamp))

}
