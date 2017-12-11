package orcus.builder

import java.util.UUID

import orcus.codec.ValueCodec
import org.apache.hadoop.hbase.client.{Durability, Delete => HDelete}
import org.apache.hadoop.hbase.security.access.Permission
import org.apache.hadoop.hbase.security.visibility.CellVisibility

import scala.collection.JavaConverters._

final class Delete(_obj: HDelete) {

  def withACL(user: String, perms: Permission): Delete = {
    _obj.setACL(user, perms)
    this
  }

  def withCellVisibility(expression: CellVisibility): Delete = {
    _obj.setCellVisibility(expression)
    this
  }

  def withDurability(durability: Durability): Delete = {
    _obj.setDurability(durability)
    this
  }

  def withId(id: String): Delete = {
    _obj.setId(id)
    this
  }

  def withAttribute(name: String, value: Array[Byte]): Delete = {
    _obj.setAttribute(name, value)
    this
  }

  def withClusterIds(clusterIds: Seq[UUID]): Delete = {
    _obj.setClusterIds(clusterIds.asJava)
    this
  }

  def withFamily(family: Array[Byte]): Delete = {
    _obj.addFamily(family)
    this
  }

  def withFamilyTo(family: Array[Byte], maxTimestamp: Long): Delete = {
    _obj.addFamily(family, maxTimestamp)
    this
  }

  def withFamilyVersion(family: Array[Byte], timestamp: Long): Delete = {
    _obj.addFamilyVersion(family, timestamp)
    this
  }

  def withColumnLatest[K](family: Array[Byte], qualifier: K)(implicit
                                                             K: ValueCodec[K]): Delete = {
    _obj.addColumn(family, K.encode(qualifier))
    this
  }

  def withColumnVersion[K](family: Array[Byte], qualifier: K, ts: Long)(
      implicit
      K: ValueCodec[K]): Delete = {
    _obj.addColumn(family, K.encode(qualifier), ts)
    this
  }

  def withColumns[K](family: Array[Byte], qualifier: K)(implicit
                                                        K: ValueCodec[K]): Delete = {
    _obj.addColumns(family, K.encode(qualifier))
    this
  }

  def withColumnsVersion[K](family: Array[Byte], qualifier: K, ts: Long)(
      implicit
      K: ValueCodec[K]): Delete = {
    _obj.addColumns(family, K.encode(qualifier), ts)
    this
  }

  def get: HDelete = _obj
}
