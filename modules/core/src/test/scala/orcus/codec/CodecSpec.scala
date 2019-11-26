package orcus.codec

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{Cell, CellBuilderType, ExtendedCellBuilderFactory}

trait CodecSpec {

  def cell(row: String, cf: String, q: String, v: Array[Byte]): Cell =
    cell(row, Bytes.toBytes(cf), Bytes.toBytes(q), v)

  def cell(row: String, cf: Array[Byte], q: Array[Byte], v: Array[Byte]): Cell = {
    val builder = ExtendedCellBuilderFactory.create(CellBuilderType.DEEP_COPY)
    builder
      .setRow(Bytes.toBytes(row))
      .setFamily(cf)
      .setQualifier(q)
      .setTimestamp(Long.MaxValue)
      .setType(Cell.Type.Put)
      .setValue(v)
      .build()
  }
}
