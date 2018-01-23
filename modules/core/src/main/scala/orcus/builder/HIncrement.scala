package orcus.builder

import java.util.UUID

import cats.data.Reader
import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Increment}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

object HIncrement {

  def withACL(user: String, perms: Permission): Reader[Increment, Increment] =
    Reader(_.setACL(user, perms))

  def withCellVisibility(expression: CellVisibility): Reader[Increment, Increment] =
    Reader(_.setCellVisibility(expression))

  def withDurability(durability: Durability): Reader[Increment, Increment] =
    Reader(_.setDurability(durability))

  def withId(id: String): Reader[Increment, Increment] =
    Reader(_.setId(id))

  def withAttribute(name: String, value: Array[Byte]): Reader[Increment, Increment] =
    Reader(_.setAttribute(name, value))

  def withClusterIds(clusterIds: Seq[UUID]): Reader[Increment, Increment] =
    Reader(_.setClusterIds(clusterIds.asJava))

  def withTTL(ttl: Long): Reader[Increment, Increment] =
    Reader(_.setTTL(ttl))

  def withColumn[K, V](family: Array[Byte], qualifier: K, amount: Long)(
      implicit
      K: ValueCodec[K]): Reader[Increment, Increment] =
    Reader(_.addColumn(family, K.encode(Option(qualifier)), amount))
}
