package orcus.builder

import java.util.UUID

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Append => HAppend}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

final class Append(_obj: HAppend) {

  def withACL(user: String, perms: Permission): Append = {
    _obj.setACL(user, perms)
    this
  }

  def withCellVisibility(expression: CellVisibility): Append = {
    _obj.setCellVisibility(expression)
    this
  }

  def withDurability(durability: Durability): Append = {
    _obj.setDurability(durability)
    this
  }

  def withId(id: String): Append = {
    _obj.setId(id)
    this
  }

  def withAttribute(name: String, value: Array[Byte]): Append = {
    _obj.setAttribute(name, value)
    this
  }

  def withClusterIds(clusterIds: Seq[UUID]): Append = {
    _obj.setClusterIds(clusterIds.asJava)
    this
  }

  def withTTL(ttl: Long): Append = {
    _obj.setTTL(ttl)
    this
  }

  def withValue[K, V](family: Array[Byte], qualifier: K, value: V)(implicit
                                                                   K: ValueCodec[K],
                                                                   V: ValueCodec[V]): Append = {
    _obj.add(family, K.encode(qualifier), V.encode(value))
    this
  }

  def withReturnResults(returnResults: Boolean): Append = {
    _obj.setReturnResults(returnResults)
    this
  }

  def get: HAppend = _obj
}
