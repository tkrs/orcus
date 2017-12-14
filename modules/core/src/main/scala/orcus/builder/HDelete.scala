package orcus.builder

import java.util.UUID

import cats.data.Reader
import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Delete}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

object HDelete {

  def withACL(user: String, perms: Permission): Reader[Delete, Delete] =
    Reader(_.setACL(user, perms))

  def withCellVisibility(expression: CellVisibility): Reader[Delete, Delete] =
    Reader(_.setCellVisibility(expression))

  def withDurability(durability: Durability): Reader[Delete, Delete] =
    Reader(_.setDurability(durability))

  def withId(id: String): Reader[Delete, Delete] =
    Reader(_.setId(id))

  def withAttribute(name: String, value: Array[Byte]): Reader[Delete, Delete] =
    Reader(_.setAttribute(name, value))

  def withClusterIds(clusterIds: Seq[UUID]): Reader[Delete, Delete] =
    Reader(_.setClusterIds(clusterIds.asJava))

  def withFamily(family: Array[Byte]): Reader[Delete, Delete] =
    Reader(_.addFamily(family))

  def withFamilyTo(family: Array[Byte], maxTimestamp: Long): Reader[Delete, Delete] =
    Reader(_.addFamily(family, maxTimestamp))

  def withFamilyVersion(family: Array[Byte], timestamp: Long): Reader[Delete, Delete] =
    Reader(_.addFamilyVersion(family, timestamp))

  def withColumnLatest[K](family: Array[Byte], qualifier: K)(
      implicit
      K: ValueCodec[K]): Reader[Delete, Delete] =
    Reader(_.addColumn(family, K.encode(qualifier)))

  def withColumnVersion[K](family: Array[Byte], qualifier: K, ts: Long)(
      implicit
      K: ValueCodec[K]): Reader[Delete, Delete] =
    Reader(_.addColumn(family, K.encode(qualifier), ts))

  def withColumns[K](family: Array[Byte], qualifier: K)(implicit
                                                        K: ValueCodec[K]): Reader[Delete, Delete] =
    Reader(_.addColumns(family, K.encode(qualifier)))

  def withColumnsVersion[K](family: Array[Byte], qualifier: K, ts: Long)(
      implicit
      K: ValueCodec[K]): Reader[Delete, Delete] =
    Reader(_.addColumns(family, K.encode(qualifier), ts))
}
