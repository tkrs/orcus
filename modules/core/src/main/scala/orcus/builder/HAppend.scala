package orcus.builder

import java.util.UUID

import cats.data.Reader
import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Append}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

object HAppend {

  def withACL(user: String, perms: Permission): Reader[Append, Append] =
    Reader(_.setACL(user, perms))

  def withCellVisibility(expression: CellVisibility): Reader[Append, Append] =
    Reader(_.setCellVisibility(expression))

  def withDurability(durability: Durability): Reader[Append, Append] =
    Reader(_.setDurability(durability))

  def withId(id: String): Reader[Append, Append] =
    Reader(_.setId(id))

  def withAttribute(name: String, value: Array[Byte]): Reader[Append, Append] =
    Reader(_.setAttribute(name, value))

  def withClusterIds(clusterIds: Seq[UUID]): Reader[Append, Append] =
    Reader(_.setClusterIds(clusterIds.asJava))

  def withTTL(ttl: Long): Reader[Append, Append] =
    Reader(_.setTTL(ttl))

  def withValue[K, V](family: Array[Byte], qualifier: K, value: V)(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V]): Reader[Append, Append] =
    Reader(_.add(family, K.encode(qualifier), V.encode(value)))

  def withReturnResults(returnResults: Boolean): Reader[Append, Append] =
    Reader(_.setReturnResults(returnResults))

}
