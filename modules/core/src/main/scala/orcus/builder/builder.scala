package orcus

import org.apache.hadoop.hbase.client.{
  Get => HGet,
  Put => HPut,
  Scan => HScan,
  Delete => HDelete,
  Append => HAppend,
  Increment => HIncrement
}
import org.apache.hadoop.hbase.util.Bytes

package object builder {
  final def get(key: String): Get             = new Get(new HGet(Bytes.toBytes(key)))
  final def scan(key: String): Scan           = new Scan(new HScan(Bytes.toBytes(key)))
  final def put(key: String): Put             = new Put(new HPut(Bytes.toBytes(key)))
  final def delete(key: String): Delete       = new Delete(new HDelete(Bytes.toBytes(key)))
  final def append(key: String): Append       = new Append(new HAppend(Bytes.toBytes(key)))
  final def increment(key: String): Increment = new Increment(new HIncrement(Bytes.toBytes(key)))
}
