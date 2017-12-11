package orcus.builder

import java.util.UUID

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Delete => HDelete}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

final class Delete(_obj: HDelete)(implicit keyCodec: ValueCodec[String]) {

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
  def withFamily(family: String): Delete = {
    _obj.addFamily(keyCodec.encode(family))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addFamily(byte[], long)]]
    */
  def withFamilyTo(family: String, maxTimestamp: Long): Delete = {
    _obj.addFamily(keyCodec.encode(family), maxTimestamp)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addFamilyVersion(byte[], long)]]
    */
  def withFamilyVersion(family: String, timestamp: Long): Delete = {
    _obj.addFamilyVersion(keyCodec.encode(family), timestamp)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addColumn(byte[], byte[])]]
    */
  def withColumnLatest[K](family: String, qualifier: K)(implicit
                                                        K: ValueCodec[K]): Delete = {
    _obj.addColumn(keyCodec.encode(family), K.encode(qualifier))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addColumn(byte[], byte[], long)]]
    */
  def withColumnVersion[K](family: String, qualifier: K, ts: Long)(implicit
                                                                   K: ValueCodec[K]): Delete = {
    _obj.addColumn(keyCodec.encode(family), K.encode(qualifier), ts)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addColumns(byte[], byte[])]]
    */
  def withColumns[K](family: String, qualifier: K)(implicit
                                                   K: ValueCodec[K]): Delete = {
    _obj.addColumns(keyCodec.encode(family), K.encode(qualifier))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Delete#addColumns(byte[], byte[], long)]]
    */
  def withColumnsVersion[K](family: String, qualifier: K, ts: Long)(implicit
                                                                    K: ValueCodec[K]): Delete = {
    _obj.addColumns(keyCodec.encode(family), K.encode(qualifier), ts)
    this
  }

  /**
    * Returns [[org.apache.hadoop.hbase.client.Delete]]
    */
  def get: HDelete = _obj
}
