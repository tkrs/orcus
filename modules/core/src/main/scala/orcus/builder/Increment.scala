package orcus.builder

import java.util.UUID

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Increment => HIncrement}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

final class Increment(_obj: HIncrement) {

  def withACL(user: String, perms: Permission): Increment = {
    _obj.setACL(user, perms)
    this
  }

  def withCellVisibility(expression: CellVisibility): Increment = {
    _obj.setCellVisibility(expression)
    this
  }

  def withDurability(durability: Durability): Increment = {
    _obj.setDurability(durability)
    this
  }

  def withId(id: String): Increment = {
    _obj.setId(id)
    this
  }

  def withAttribute(name: String, value: Array[Byte]): Increment = {
    _obj.setAttribute(name, value)
    this
  }

  def withClusterIds(clusterIds: Seq[UUID]): Increment = {
    _obj.setClusterIds(clusterIds.asJava)
    this
  }

  def withTTL(ttl: Long): Increment = {
    _obj.setTTL(ttl)
    this
  }

  def withColumn[K, V](family: Array[Byte], qualifier: K, amount: Long)(
      implicit
      K: ValueCodec[K]): Increment = {
    _obj.addColumn(family, K.encode(qualifier), amount)
    this
  }

  def get: HIncrement = _obj
}
