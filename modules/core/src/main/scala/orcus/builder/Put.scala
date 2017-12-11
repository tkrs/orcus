package orcus.builder

import java.util.UUID

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Put => HPut}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

final class Put(_obj: HPut) {

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#setACL]]
    */
  def withACL(user: String, perms: Permission): Put = {
    _obj.setACL(user, perms)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#setCellVisibility]]
    */
  def withCellVisibility(expression: CellVisibility): Put = {
    _obj.setCellVisibility(expression)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#setDurability]]
    */
  def withDurability(durability: Durability): Put = {
    _obj.setDurability(durability)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#setId]]
    */
  def withId(id: String): Put = {
    _obj.setId(id)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#setAttribute]]
    */
  def withAttribute(name: String, value: Array[Byte]): Put = {
    _obj.setAttribute(name, value)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#setClusterIds]]
    */
  def withClusterIds(clusterIds: Seq[UUID]): Put = {
    _obj.setClusterIds(clusterIds.asJava)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#setTTL]]
    */
  def withTTL(ttl: Long): Put = {
    _obj.setTTL(ttl)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#addColumn(byte[], byte[], byte[])]]
    */
  def withColumn[K, V](family: Array[Byte], qualifier: K, value: V)(implicit
                                                                    K: ValueCodec[K],
                                                                    V: ValueCodec[V]): Put = {
    _obj.addColumn(family, K.encode(qualifier), V.encode(value))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#addColumn(byte[], byte[], long, byte[])]]
    */
  def withColumnVersion[K, V](family: Array[Byte], qualifier: K, ts: Long, value: V)(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V]): Put = {
    _obj.addColumn(family, K.encode(qualifier), ts, V.encode(value))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#addImmutable(byte[], byte[], byte[])]]
    */
  def withImmutable[K, V](family: Array[Byte], qualifier: K, value: V)(implicit
                                                                       K: ValueCodec[K],
                                                                       V: ValueCodec[V]): Put = {
    _obj.addImmutable(family, K.encode(qualifier), V.encode(value))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Put#addImmutable(byte[], byte[], long, byte[])]]
    */
  def withImmutableVersion[K, V](family: Array[Byte], qualifier: K, ts: Long, value: V)(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V]): Put = {
    _obj.addImmutable(family, K.encode(qualifier), ts, V.encode(value))
    this
  }

  /**
    * Returns [[org.apache.hadoop.hbase.client.Put]]
    */
  def get: HPut = _obj
}
