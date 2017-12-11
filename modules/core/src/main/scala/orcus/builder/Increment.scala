package orcus.builder

import java.util.UUID

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Increment => HIncrement}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

final class Increment(_obj: HIncrement)(implicit keyCodec: ValueCodec[String]) {

  /**
    * @see [[org.apache.hadoop.hbase.client.Increment#setACL]]
    */
  def withACL(user: String, perms: Permission): Increment = {
    _obj.setACL(user, perms)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Increment#setCellVisibility]]
    */
  def withCellVisibility(expression: CellVisibility): Increment = {
    _obj.setCellVisibility(expression)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Increment#setDurability]]
    */
  def withDurability(durability: Durability): Increment = {
    _obj.setDurability(durability)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Increment#setId]]
    */
  def withId(id: String): Increment = {
    _obj.setId(id)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Increment#setAttribute]]
    */
  def withAttribute(name: String, value: Array[Byte]): Increment = {
    _obj.setAttribute(name, value)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Increment#setClusterIds]]
    */
  def withClusterIds(clusterIds: Seq[UUID]): Increment = {
    _obj.setClusterIds(clusterIds.asJava)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Increment#setTTL]]
    */
  def withTTL(ttl: Long): Increment = {
    _obj.setTTL(ttl)
    this
  }

  /**
    * @see [[org.apache.hadoop.hbase.client.Increment#addColumn]]
    */
  def withColumn[K, V](family: String, qualifier: K, amount: Long)(implicit
                                                                   K: ValueCodec[K]): Increment = {
    _obj.addColumn(keyCodec.encode(family), K.encode(qualifier), amount)
    this
  }

  /**
    * Returns [[org.apache.hadoop.hbase.client.Increment]]
    */
  def get: HIncrement = _obj
}
