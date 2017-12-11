package orcus.builder

import java.util.UUID

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Delete => HDelete}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

final class Delete(_obj: HDelete) {

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#setACL]]
    */
  def withACL(user: String, perms: Permission): Delete = {
    _obj.setACL(user, perms)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#setCellVisibility]]
    */
  def withCellVisibility(expression: CellVisibility): Delete = {
    _obj.setCellVisibility(expression)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#setDurability]]
    */
  def withDurability(durability: Durability): Delete = {
    _obj.setDurability(durability)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#setId]]
    */
  def withId(id: String): Delete = {
    _obj.setId(id)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#setAttribute]]
    */
  def withAttribute(name: String, value: Array[Byte]): Delete = {
    _obj.setAttribute(name, value)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#setClusterIds]]
    */
  def withClusterIds(clusterIds: Seq[UUID]): Delete = {
    _obj.setClusterIds(clusterIds.asJava)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addFamily(byte[])]]
    */
  def withFamily(family: Array[Byte]): Delete = {
    _obj.addFamily(family)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addFamily(byte[], long)]]
    */
  def withFamilyTo(family: Array[Byte], maxTimestamp: Long): Delete = {
    _obj.addFamily(family, maxTimestamp)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addFamilyVersion(byte[], long)]]
    */
  def withFamilyVersion(family: Array[Byte], timestamp: Long): Delete = {
    _obj.addFamilyVersion(family, timestamp)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addColumn(byte[], byte[])]]
    */
  def withColumnLatest[K](family: Array[Byte], qualifier: K)(implicit
                                                             K: ValueCodec[K]): Delete = {
    _obj.addColumn(family, K.encode(qualifier))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addColumn(byte[], byte[], long)]]
    */
  def withColumnVersion[K](family: Array[Byte], qualifier: K, ts: Long)(
      implicit
      K: ValueCodec[K]): Delete = {
    _obj.addColumn(family, K.encode(qualifier), ts)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addColumns(byte[], byte[])]]
    */
  def withColumns[K](family: Array[Byte], qualifier: K)(implicit
                                                        K: ValueCodec[K]): Delete = {
    _obj.addColumns(family, K.encode(qualifier))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addColumns(byte[], byte[], long)]]
    */
  def withColumnsVersion[K](family: Array[Byte], qualifier: K, ts: Long)(
      implicit
      K: ValueCodec[K]): Delete = {
    _obj.addColumns(family, K.encode(qualifier), ts)
    this
  }

  /**
    * Returns [[org.apache.hadoop.hbase.client.Delete]]
    */
  def get: HDelete = _obj
}
