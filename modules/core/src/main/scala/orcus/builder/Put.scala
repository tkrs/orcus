package orcus.builder

import java.util.UUID

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Put => HPut}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

final class Put(_obj: HPut) {

  def withACL(user: String, perms: Permission): Put = {
    _obj.setACL(user, perms)
    this
  }

  def withCellVisibility(expression: CellVisibility): Put = {
    _obj.setCellVisibility(expression)
    this
  }

  def withDurability(durability: Durability): Put = {
    _obj.setDurability(durability)
    this
  }

  def withId(id: String): Put = {
    _obj.setId(id)
    this
  }

  def withAttribute(name: String, value: Array[Byte]): Put = {
    _obj.setAttribute(name, value)
    this
  }

  def withClusterIds(clusterIds: Seq[UUID]): Put = {
    _obj.setClusterIds(clusterIds.asJava)
    this
  }

  def withTTL(ttl: Long): Put = {
    _obj.setTTL(ttl)
    this
  }

  def withColumn[K, V](family: Array[Byte], qualifier: K, value: V)(implicit
                                                                    K: ValueCodec[K],
                                                                    V: ValueCodec[V]): Put = {
    _obj.addColumn(family, K.encode(qualifier), V.encode(value))
    this
  }

  def withColumnVersion[K, V](family: Array[Byte], qualifier: K, ts: Long, value: V)(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V]): Put = {
    _obj.addColumn(family, K.encode(qualifier), ts, V.encode(value))
    this
  }

  def withImmutable[K, V](family: Array[Byte], qualifier: K, value: V)(implicit
                                                                       K: ValueCodec[K],
                                                                       V: ValueCodec[V]): Put = {
    _obj.addImmutable(family, K.encode(qualifier), V.encode(value))
    this
  }

  def withImmutableVersion[K, V](family: Array[Byte], qualifier: K, ts: Long, value: V)(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V]): Put = {
    _obj.addImmutable(family, K.encode(qualifier), ts, V.encode(value))
    this
  }

  def get: HPut = _obj
}
