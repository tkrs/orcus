package orcus.codec

import cats.Eval
import orcus.internal.ScalaVersionSpecifics._
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import shapeless.labelled._
import shapeless._

import scala.annotation.tailrec
import scala.collection.mutable

trait Decoder[A] extends Serializable { self =>

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

object Decoder extends HighPriorityDecoder {

  @inline def apply[A](implicit A: Decoder[A]): Decoder[A] = A

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

trait HighPriorityDecoder extends LowPriorityDecoder {

  implicit def decodeOption[A](implicit A: Decoder[A]): Decoder[Option[A]] =
    new Decoder[Option[A]] {
      def apply(result: Result): Either[Throwable, Option[A]] =
        if (result.isEmpty) Right(None)
        else
          A(result) match {
            case Right(v) => Right(Some(v))
            case Left(e)  => Left(e)
          }
    }

  implicit def decodeMapLike[M[_, _] <: Map[String, V], V](
    implicit
    K: ValueCodec[String],
    V: FamilyDecoder[V],
    factory: Factory[(String, V), M[String, V]]
  ): Decoder[M[String, V]] =
    new Decoder[M[String, V]] {

      def apply(result: Result): Either[Throwable, M[String, V]] = {
        val builder = factory.newBuilder
        val map     = result.getMap
        if (map == null) Right(builder.result())
        else {
          val keys = map.keySet().iterator()

          @tailrec def loop(acc: mutable.Builder[(String, V), M[String, V]]): Either[Throwable, M[String, V]] =
            if (!keys.hasNext) Right(acc.result())
            else {
              val key = keys.next
              K.decode(key) match {
                case Right(k) =>
                  V(result.getFamilyMap(key)) match {
                    case Right(v) =>
                      loop(builder += k -> v)
                    case Left(e) =>
                      Left(e)
                  }
                case Left(e) => Left(e)
              }
            }

          loop(builder)
        }
      }
    }
}

trait LowPriorityDecoder {

  implicit val decodeHNil: Decoder[HNil] = new Decoder[HNil] {
    def apply(result: Result): Either[Throwable, HNil] = Right(HNil)
  }

  implicit def decodeLabelledHCons[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: FamilyDecoder[H],
    T: Lazy[Decoder[T]]
  ): Decoder[FieldType[K, H] :: T] =
    new Decoder[FieldType[K, H] :: T] {
      def apply(result: Result): Either[Throwable, FieldType[K, H] :: T] =
        T.value(result) match {
          case Right(t) =>
            val k = Bytes.toBytes(K.value.name)
            H(result.getFamilyMap(k)) match {
              case Right(h) => Right(field[K](h) :: t)
              case Left(e)  => Left(e)
            }
          case Left(e) =>
            Left(e)
        }
    }

  implicit def decodeLabelledGen[H <: HList, A](
    implicit
    gen: LabelledGeneric.Aux[A, H],
    A: Lazy[Decoder[H]]
  ): Decoder[A] =
    new Decoder[A] {
      def apply(result: Result): Either[Throwable, A] =
        A.value(result) match {
          case Right(v) => Right(gen.from(v))
          case Left(e)  => Left(e)
        }
    }
}
