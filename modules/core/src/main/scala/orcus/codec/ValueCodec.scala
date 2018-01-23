package orcus.codec

import org.apache.hadoop.hbase.util.Bytes

trait ValueCodec[A] { self =>

  def encode(a: Option[A]): Array[Byte]
  def decode(bytes: Array[Byte]): Option[A]

  def imap[B](fa: B => A, fb: A => B): ValueCodec[B] = new ValueCodec[B] {
    def encode(a: Option[B]): Array[Byte]     = self.encode(a.map(fa))
    def decode(bytes: Array[Byte]): Option[B] = self.decode(bytes).map(fb)
  }
}

object ValueCodec {

  def apply[A](implicit A: ValueCodec[A]): ValueCodec[A] = A

  implicit val codecForBytes: ValueCodec[Array[Byte]] = new ValueCodec[Array[Byte]] {
    def encode(a: Option[Array[Byte]]): Array[Byte]     = a.orEmpty
    def decode(bytes: Array[Byte]): Option[Array[Byte]] = Option(bytes)
  }

  implicit val codecForBoolean: ValueCodec[Boolean] = new ValueCodec[Boolean] {
    def encode(a: Option[Boolean]): Array[Byte] =
      a.map(Bytes.toBytes).orEmpty
    def decode(bytes: Array[Byte]): Option[Boolean] =
      if (bytes.isEmpty) None else Some(Bytes.toBoolean(bytes))
  }

  implicit val codecForShort: ValueCodec[Short] = new ValueCodec[Short] {
    def encode(a: Option[Short]): Array[Byte] =
      a.map(Bytes.toBytes).orEmpty
    def decode(bytes: Array[Byte]): Option[Short] =
      if (bytes.isEmpty) None else Some(Bytes.toShort(bytes))
  }

  implicit val codecForInt: ValueCodec[Int] = new ValueCodec[Int] {
    def encode(a: Option[Int]): Array[Byte] =
      a.map(Bytes.toBytes).orEmpty
    def decode(bytes: Array[Byte]): Option[Int] =
      if (bytes.isEmpty) None else Some(Bytes.toInt(bytes))
  }

  implicit val codecForLong: ValueCodec[Long] = new ValueCodec[Long] {
    def encode(a: Option[Long]): Array[Byte] =
      a.map(Bytes.toBytes).orEmpty
    def decode(bytes: Array[Byte]): Option[Long] =
      if (bytes.isEmpty) None else Some(Bytes.toLong(bytes))
  }

  implicit val codecForFloat: ValueCodec[Float] = new ValueCodec[Float] {
    def encode(a: Option[Float]): Array[Byte] =
      a.map(Bytes.toBytes).orEmpty
    def decode(bytes: Array[Byte]): Option[Float] =
      if (bytes.isEmpty) None else Some(Bytes.toFloat(bytes))
  }

  implicit val codecForDouble: ValueCodec[Double] = new ValueCodec[Double] {
    def encode(a: Option[Double]): Array[Byte] =
      a.map(Bytes.toBytes).orEmpty
    def decode(bytes: Array[Byte]): Option[Double] =
      if (bytes.isEmpty) None else Some(Bytes.toDouble(bytes))
  }

  implicit val codecForBigInt: ValueCodec[BigDecimal] = new ValueCodec[BigDecimal] {
    def encode(a: Option[BigDecimal]): Array[Byte] =
      a.map(v => Bytes.toBytes(v.bigDecimal)).orEmpty
    def decode(bytes: Array[Byte]): Option[BigDecimal] =
      if (bytes.isEmpty) None else Some(BigDecimal(Bytes.toBigDecimal(bytes)))
  }

  implicit val codecForString: ValueCodec[String] = new ValueCodec[String] {
    def encode(a: Option[String]): Array[Byte] =
      a.map(Bytes.toBytes).orEmpty
    def decode(bytes: Array[Byte]): Option[String] =
      if (bytes == null) None else Option(Bytes.toString(bytes))
  }

  implicit def codecForOption[A](implicit A: ValueCodec[A]): ValueCodec[Option[A]] =
    new ValueCodec[Option[A]] {
      def encode(a: Option[Option[A]]): Array[Byte] =
        a match {
          case Some(v) => A.encode(v)
          case _       => Array.emptyByteArray
        }
      def decode(bytes: Array[Byte]): Option[Option[A]] =
        if (bytes.isEmpty) Some(None) else Option(A.decode(bytes))
    }
}
