package orcus

import org.apache.hadoop.hbase.client.{
  Get => HGet,
  Put => HPut,
  Scan => HScan,
  Delete => HDelete,
  Append => HAppend,
  Increment => HIncrement
}

package object builder {
  final def get(row: Array[Byte]): Get                    = new Get(new HGet(row))
  final def scan(): Scan                                  = new Scan(new HScan())
  final def scanRow(startRow: Array[Byte]): Scan          = new Scan(new HScan(startRow))
  final def put(row: Array[Byte]): Put                    = new Put(new HPut(row))
  final def putTimestamp(row: Array[Byte], ts: Long): Put = new Put(new HPut(row, ts))
  final def delete(row: Array[Byte]): Delete              = new Delete(new HDelete(row))
  final def append(row: Array[Byte]): Append              = new Append(new HAppend(row))
  final def increment(row: Array[Byte]): Increment        = new Increment(new HIncrement(row))
}
