package orcus.codec

import org.apache.hadoop.hbase.util.Bytes

trait ValueCodec[A] {
  def encode(a: A): Array[Byte]
  def decode(bytes: Array[Byte]): A
}

object ValueCodec {

  def apply[A](implicit A: ValueCodec[A]): ValueCodec[A] = A

  implicit val codecForBytes: ValueCodec[Array[Byte]] = new ValueCodec[Array[Byte]] {
    def encode(a: Array[Byte]): Array[Byte]     = a
    def decode(bytes: Array[Byte]): Array[Byte] = bytes
  }

  implicit val codecForBoolean: ValueCodec[Boolean] = new ValueCodec[Boolean] {
    def encode(a: Boolean): Array[Byte]     = Bytes.toBytes(a)
    def decode(bytes: Array[Byte]): Boolean = Bytes.toBoolean(bytes)
  }

  implicit val codecForShort: ValueCodec[Short] = new ValueCodec[Short] {
    def encode(a: Short): Array[Byte]     = Bytes.toBytes(a)
    def decode(bytes: Array[Byte]): Short = Bytes.toShort(bytes)
  }

  implicit val codecForInt: ValueCodec[Int] = new ValueCodec[Int] {
    def encode(a: Int): Array[Byte]     = Bytes.toBytes(a)
    def decode(bytes: Array[Byte]): Int = Bytes.toInt(bytes)
  }

  implicit val codecForLong: ValueCodec[Long] = new ValueCodec[Long] {
    def encode(a: Long): Array[Byte]     = Bytes.toBytes(a)
    def decode(bytes: Array[Byte]): Long = Bytes.toLong(bytes)
  }

  implicit val codecForFloat: ValueCodec[Float] = new ValueCodec[Float] {
    def encode(a: Float): Array[Byte]     = Bytes.toBytes(a)
    def decode(bytes: Array[Byte]): Float = Bytes.toFloat(bytes)
  }

  implicit val codecForDouble: ValueCodec[Double] = new ValueCodec[Double] {
    def encode(a: Double): Array[Byte]     = Bytes.toBytes(a)
    def decode(bytes: Array[Byte]): Double = Bytes.toDouble(bytes)
  }

  implicit val codecForBigInt: ValueCodec[BigDecimal] = new ValueCodec[BigDecimal] {
    def encode(a: BigDecimal): Array[Byte]     = Bytes.toBytes(a.bigDecimal)
    def decode(bytes: Array[Byte]): BigDecimal = BigDecimal(Bytes.toBigDecimal(bytes))
  }

  implicit val codecForString: ValueCodec[String] = new ValueCodec[String] {
    def encode(a: String): Array[Byte]     = Bytes.toBytes(a)
    def decode(bytes: Array[Byte]): String = Bytes.toString(bytes)
  }
}
