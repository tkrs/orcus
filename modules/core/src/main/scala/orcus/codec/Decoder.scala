package orcus.codec

import cats.Eval
import cats.instances.either._
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import shapeless.labelled._
import shapeless._

trait Decoder[A] { self =>

  def apply(result: Result): Either[Throwable, A]

  def flatMap[B](f: A => Decoder[B]): Decoder[B] = new Decoder[B] {
    def apply(result: Result): Either[Throwable, B] = self(result) match {
      case Right(a)    => f(a)(result)
      case l @ Left(_) => l.asInstanceOf[Either[Throwable, B]]
    }
  }

  def map[B](f: A => B): Decoder[B] = new Decoder[B] {
    def apply(result: Result): Either[Throwable, B] = self(result) match {
      case Right(a)    => Right(f(a))
      case l @ Left(_) => l.asInstanceOf[Either[Throwable, B]]
    }
  }

  def mapF[B](f: A => Either[Throwable, B]): Decoder[B] = new Decoder[B] {
    def apply(result: Result): Either[Throwable, B] = self(result) match {
      case Right(a)    => f(a)
      case l @ Left(_) => l.asInstanceOf[Either[Throwable, B]]
    }
  }
}

object Decoder extends Decoder1 {

  def apply[A](implicit A: Decoder[A]): Decoder[A] = A

  def pure[A](a: A): Decoder[A] = new Decoder[A] {
    def apply(result: Result): Either[Throwable, A] = Right(a)
  }

  def eval[A](a: Eval[A]): Decoder[A] = new Decoder[A] {
    def apply(result: Result): Either[Throwable, A] = Right(a.value)
  }

  def liftF[A](a: Either[Throwable, A]): Decoder[A] = new Decoder[A] {
    def apply(result: Result): Either[Throwable, A] = a
  }
}

trait Decoder1 extends Decoder2 {

  implicit def decodeHNil: Decoder[HNil] = new Decoder[HNil] {
    def apply(result: Result): Either[Throwable, HNil] = Right(HNil)
  }

  implicit def decodeLabelledHList[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: FamilyDecoder[H],
      T: Lazy[Decoder[T]]): Decoder[FieldType[K, H] :: T] =
    new Decoder[FieldType[K, H] :: T] {
      def apply(result: Result): Either[Throwable, FieldType[K, H] :: T] = {
        val k = Bytes.toBytes(K.value.name)
        val h = orcus.result.getFamily[H, Either[Throwable, ?]](result, k)
        h match {
          case Right(v0) =>
            T.value(result) match {
              case Right(t) => Right(field[K](v0) :: t)
              case Left(e)  => Left(e)
            }
          case Left(e) =>
            Left(e)
        }
      }
    }

  implicit def decodeHCons[H <: HList, A0](implicit
                                           gen: LabelledGeneric.Aux[A0, H],
                                           A: Lazy[Decoder[H]]): Decoder[A0] =
    new Decoder[A0] {
      def apply(result: Result): Either[Throwable, A0] =
        A.value(result) match {
          case Right(v) => Right(gen.from(v))
          case Left(e)  => Left(e)
        }
    }
}

trait Decoder2 {

  implicit def decodeOption[A](implicit A: Decoder[A]): Decoder[Option[A]] =
    new Decoder[Option[A]] {
      def apply(result: Result): Either[Throwable, Option[A]] = {
        if (result.isEmpty) Right(None)
        else
          A.apply(result) match {
            case Right(v) => Right(Some(v))
            case Left(e)  => Left(e)
          }
      }
    }
}
