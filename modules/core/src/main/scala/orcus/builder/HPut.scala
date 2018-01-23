package orcus.builder

import java.util.UUID

import cats.data.Reader
import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Put}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

object HPut {

  def withACL(user: String, perms: Permission): Reader[Put, Put] =
    Reader(_.setACL(user, perms))

  def withCellVisibility(expression: CellVisibility): Reader[Put, Put] =
    Reader(_.setCellVisibility(expression))

  def withDurability(durability: Durability): Reader[Put, Put] =
    Reader(_.setDurability(durability))

  def withId(id: String): Reader[Put, Put] =
    Reader(_.setId(id))

  def withAttribute(name: String, value: Array[Byte]): Reader[Put, Put] =
    Reader(_.setAttribute(name, value))

  def withClusterIds(clusterIds: Seq[UUID]): Reader[Put, Put] =
    Reader(_.setClusterIds(clusterIds.asJava))

  def withTTL(ttl: Long): Reader[Put, Put] =
    Reader(_.setTTL(ttl))

  def withColumn[K, V](family: Array[Byte], qualifier: K, value: V)(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V]): Reader[Put, Put] =
    Reader(_.addColumn(family, K.encode(Option(qualifier)), V.encode(Option(value))))

  def withColumnVersion[K, V](family: Array[Byte], qualifier: K, ts: Long, value: V)(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V]): Reader[Put, Put] =
    Reader(_.addColumn(family, K.encode(Option(qualifier)), ts, V.encode(Option(value))))

  def withImmutable[K, V](family: Array[Byte], qualifier: K, value: V)(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V]): Reader[Put, Put] =
    Reader(_.addImmutable(family, K.encode(Option(qualifier)), V.encode(Option(value))))

  def withImmutableVersion[K, V](family: Array[Byte], qualifier: K, ts: Long, value: V)(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V]): Reader[Put, Put] =
    Reader(_.addImmutable(family, K.encode(Option(qualifier)), ts, V.encode(Option(value))))

}
