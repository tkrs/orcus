package orcus.builder

import java.util.UUID

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Append => HAppend}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

final class Append(_obj: HAppend)(implicit keyCodec: ValueCodec[String]) {

  /**
    * @see [[org.apache.hadoop.hbase.client.Append#setACL]]
    */
  def withACL(user: String, perms: Permission): Append = {
    _obj.setACL(user, perms)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Append#setCellVisibility]]
    */
  def withCellVisibility(expression: CellVisibility): Append = {
    _obj.setCellVisibility(expression)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Append#setDurability]]
    */
  def withDurability(durability: Durability): Append = {
    _obj.setDurability(durability)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Append#setId]]
    */
  def withId(id: String): Append = {
    _obj.setId(id)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Append#setAttribute]]
    */
  def withAttribute(name: String, value: Array[Byte]): Append = {
    _obj.setAttribute(name, value)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Append#withClusterIds]]
    */
  def withClusterIds(clusterIds: Seq[UUID]): Append = {
    _obj.setClusterIds(clusterIds.asJava)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Append#setTTL]]
    */
  def withTTL(ttl: Long): Append = {
    _obj.setTTL(ttl)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Append#add(byte[], byte[], byte[])]]
    */
  def withValue[K, V](family: String, qualifier: K, value: V)(implicit
                                                              K: ValueCodec[K],
                                                              V: ValueCodec[V]): Append = {
    _obj.add(keyCodec.encode(family), K.encode(qualifier), V.encode(value))
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Append#setReturnResults]]
    */
  def withReturnResults(returnResults: Boolean): Append = {
    _obj.setReturnResults(returnResults)
    this
  }

  def get: HAppend = _obj
}
