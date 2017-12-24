package orcus.codec

import org.apache.hadoop.hbase.util.Bytes

trait ValueCodec[A] { self =>

  def encode(a: Option[A]): Option[Array[Byte]]
  def decode(bytes: Option[Array[Byte]]): Option[A]

  def imap[B](fa: B => A, fb: A => B): ValueCodec[B] = new ValueCodec[B] {
    def encode(a: Option[B]): Option[Array[Byte]]     = self.encode(a.map(fa))
    def decode(bytes: Option[Array[Byte]]): Option[B] = self.decode(bytes).map(fb)
  }
}

object ValueCodec {

  def apply[A](implicit A: ValueCodec[A]): ValueCodec[A] = A

  implicit val codecForBytes: ValueCodec[Array[Byte]] = new ValueCodec[Array[Byte]] {
    def encode(a: Option[Array[Byte]]): Option[Array[Byte]]     = a
    def decode(bytes: Option[Array[Byte]]): Option[Array[Byte]] = bytes
  }

  implicit val codecForBoolean: ValueCodec[Boolean] = new ValueCodec[Boolean] {
    def encode(a: Option[Boolean]): Option[Array[Byte]] =
      a.map(Bytes.toBytes)
    def decode(bytes: Option[Array[Byte]]): Option[Boolean] =
      bytes.map(Bytes.toBoolean)
  }

  implicit val codecForShort: ValueCodec[Short] = new ValueCodec[Short] {
    def encode(a: Option[Short]): Option[Array[Byte]] =
      a.map(Bytes.toBytes)
    def decode(bytes: Option[Array[Byte]]): Option[Short] =
      bytes.map(Bytes.toShort)
  }

  implicit val codecForInt: ValueCodec[Int] = new ValueCodec[Int] {
    def encode(a: Option[Int]): Option[Array[Byte]] =
      a.map(Bytes.toBytes)
    def decode(bytes: Option[Array[Byte]]): Option[Int] =
      bytes.map(Bytes.toInt)
  }

  implicit val codecForLong: ValueCodec[Long] = new ValueCodec[Long] {
    def encode(a: Option[Long]): Option[Array[Byte]] =
      a.map(Bytes.toBytes)
    def decode(bytes: Option[Array[Byte]]): Option[Long] =
      bytes.map(Bytes.toLong)
  }

  implicit val codecForFloat: ValueCodec[Float] = new ValueCodec[Float] {
    def encode(a: Option[Float]): Option[Array[Byte]] =
      a.map(Bytes.toBytes)
    def decode(bytes: Option[Array[Byte]]): Option[Float] =
      bytes.map(Bytes.toFloat)
  }

  implicit val codecForDouble: ValueCodec[Double] = new ValueCodec[Double] {
    def encode(a: Option[Double]): Option[Array[Byte]] =
      a.map(Bytes.toBytes)
    def decode(bytes: Option[Array[Byte]]): Option[Double] =
      bytes.map(Bytes.toDouble)
  }

  implicit val codecForBigInt: ValueCodec[BigDecimal] = new ValueCodec[BigDecimal] {
    def encode(a: Option[BigDecimal]): Option[Array[Byte]] =
      a.map(v => Bytes.toBytes(v.bigDecimal))
    def decode(bytes: Option[Array[Byte]]): Option[BigDecimal] =
      bytes.map(v => BigDecimal(Bytes.toBigDecimal(v)))
  }

  implicit val codecForString: ValueCodec[String] = new ValueCodec[String] {
    def encode(a: Option[String]): Option[Array[Byte]] =
      a.map(Bytes.toBytes)
    def decode(bytes: Option[Array[Byte]]): Option[String] =
      bytes.map(Bytes.toString)
  }

  implicit def codecForOption[A](implicit A: ValueCodec[A]): ValueCodec[Option[A]] =
    new ValueCodec[Option[A]] {
      def encode(a: Option[Option[A]]): Option[Array[Byte]] =
        a match {
          case Some(v) => A.encode(v)
          case _       => None
        }
      def decode(bytes: Option[Array[Byte]]): Option[Option[A]] =
        if (bytes.isEmpty) Some(None) else Some(A.decode(bytes))
    }
}
