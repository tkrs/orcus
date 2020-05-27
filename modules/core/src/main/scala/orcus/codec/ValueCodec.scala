package orcus.codec

import cats.syntax.either._
import org.apache.hadoop.hbase.util.Bytes

trait ValueCodec[A] { self =>

  def encode(a: A): Array[Byte]
  def decode(bytes: Array[Byte]): ValueCodec.Result[A]

  final def imap[B](fa: B => A, fb: A => B): ValueCodec[B] =
    new ValueCodec[B] {
      def encode(a: B): Array[Byte] = self.encode(fa(a))
      def decode(bytes: Array[Byte]): ValueCodec.Result[B] = self.decode(bytes).map(fb)
    }
}

object ValueCodec {
  type Result[A] = Either[Throwable, A]

  def apply[A](implicit A: ValueCodec[A]): ValueCodec[A] = A

  implicit val codecForBytes: ValueCodec[Array[Byte]] = new ValueCodec[Array[Byte]] {
    def encode(a: Array[Byte]): Array[Byte] = a
    def decode(bytes: Array[Byte]): Result[Array[Byte]] = Right(bytes)
  }

  implicit val codecForBoolean: ValueCodec[Boolean] = new ValueCodec[Boolean] {
    def encode(a: Boolean): Array[Byte] = Bytes.toBytes(a)

    def decode(bytes: Array[Byte]): Result[Boolean] =
      Either.catchNonFatal(Bytes.toBoolean(bytes))
  }

  implicit val codecForShort: ValueCodec[Short] = new ValueCodec[Short] {
    def encode(a: Short): Array[Byte] = Bytes.toBytes(a)

    def decode(bytes: Array[Byte]): Result[Short] =
      Either.catchNonFatal(Bytes.toShort(bytes))
  }

  implicit val codecForInt: ValueCodec[Int] = new ValueCodec[Int] {
    def encode(a: Int): Array[Byte] = Bytes.toBytes(a)

    def decode(bytes: Array[Byte]): Result[Int] =
      Either.catchNonFatal(Bytes.toInt(bytes))
  }

  implicit val codecForLong: ValueCodec[Long] = new ValueCodec[Long] {
    def encode(a: Long): Array[Byte] = Bytes.toBytes(a)

    def decode(bytes: Array[Byte]): Result[Long] =
      Either.catchNonFatal(Bytes.toLong(bytes))
  }

  implicit val codecForFloat: ValueCodec[Float] = new ValueCodec[Float] {
    def encode(a: Float): Array[Byte] = Bytes.toBytes(a)

    def decode(bytes: Array[Byte]): Result[Float] =
      Either.catchNonFatal(Bytes.toFloat(bytes))
  }

  implicit val codecForDouble: ValueCodec[Double] = new ValueCodec[Double] {
    def encode(a: Double): Array[Byte] = Bytes.toBytes(a)

    def decode(bytes: Array[Byte]): Result[Double] =
      Either.catchNonFatal(Bytes.toDouble(bytes))
  }

  implicit val codecForBigInt: ValueCodec[BigDecimal] = new ValueCodec[BigDecimal] {
    def encode(a: BigDecimal): Array[Byte] = Bytes.toBytes(a.bigDecimal)

    def decode(bytes: Array[Byte]): Result[BigDecimal] =
      Either.catchNonFatal(BigDecimal(Bytes.toBigDecimal(bytes)))
  }

  implicit val codecForString: ValueCodec[String] = new ValueCodec[String] {
    def encode(a: String): Array[Byte] = Bytes.toBytes(a)

    def decode(bytes: Array[Byte]): Result[String] =
      Either.catchNonFatal(Bytes.toString(bytes))
  }

  implicit val codecForOptionString: ValueCodec[Option[String]] =
    new ValueCodec[Option[String]] {
      private[this] val c = ValueCodec[String]

      def encode(a: Option[String]): Array[Byte] =
        a.fold(Array.emptyByteArray)(c.encode)

      def decode(bytes: Array[Byte]): Result[Option[String]] =
        if (bytes == null) Right(None) else c.decode(bytes).map(Option.apply)
    }

  implicit def codecForOption[A](implicit A: ValueCodec[A]): ValueCodec[Option[A]] =
    new ValueCodec[Option[A]] {
      def encode(a: Option[A]): Array[Byte] = a.fold(Array.emptyByteArray)(A.encode)

      def decode(bytes: Array[Byte]): Result[Option[A]] =
        if (bytes == null || bytes.isEmpty) Right(None) else A.decode(bytes).map(Option.apply)
    }
}
