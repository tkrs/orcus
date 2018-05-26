package orcus.codec

import java.util.{NavigableMap => NMap}

import cats.Eval
import org.apache.hadoop.hbase.util.Bytes
import shapeless.labelled._
import shapeless._

import scala.annotation.tailrec
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

trait FamilyDecoder[A] { self =>

  def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, A]

  def flatMap[B](f: A => FamilyDecoder[B]): FamilyDecoder[B] = new FamilyDecoder[B] {
    def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, B] = {
      self(map) match {
        case Right(a)    => f(a)(map)
        case l @ Left(_) => l.asInstanceOf[Either[Throwable, B]]
      }
    }
  }

  def map[B](f: A => B): FamilyDecoder[B] = new FamilyDecoder[B] {
    def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, B] =
      self(map) match {
        case Right(a)    => Right(f(a))
        case l @ Left(_) => l.asInstanceOf[Either[Throwable, B]]
      }
  }

  def mapF[B](f: A => Either[Throwable, B]): FamilyDecoder[B] = new FamilyDecoder[B] {
    def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, B] =
      self(map) match {
        case Right(a)    => f(a)
        case l @ Left(_) => l.asInstanceOf[Either[Throwable, B]]
      }
  }
}

object FamilyDecoder extends FamilyDecoder1 {

  def apply[A](implicit A: FamilyDecoder[A]): FamilyDecoder[A] = A

  def pure[A](a: A): FamilyDecoder[A] = new FamilyDecoder[A] {
    def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, A] =
      Right(a)
  }

  def eval[A](a: Eval[A]): FamilyDecoder[A] = new FamilyDecoder[A] {
    def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, A] =
      Right(a.value)
  }

  def liftF[A](a: Either[Throwable, A]): FamilyDecoder[A] = new FamilyDecoder[A] {
    def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, A] = a
  }
}

private[codec] trait FamilyDecoder1 extends FamilyDecoder2 {

  implicit def decodeMapLike[M[_, _] <: Map[K, V], K, V](
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V],
      cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): FamilyDecoder[M[K, V]] =
    new FamilyDecoder[M[K, V]] {

      def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, M[K, V]] = {
        val builder = cbf()
        if (map == null)
          Right(builder.result())
        else {
          val entries = map.entrySet().iterator()

          @tailrec def loop(acc: mutable.Builder[(K, V), M[K, V]]): Either[Throwable, M[K, V]] = {
            if (!entries.hasNext) Right(acc.result())
            else {
              val entry = entries.next()
              val key   = entry.getKey
              val value = entry.getValue

              K.decode(key) match {
                case Right(k) =>
                  V.decode(value) match {
                    case Right(v) =>
                      loop(if (v == null) builder else builder += k -> v)
                    case Left(_) =>
                      loop(builder)
                  }
                case Left(e) => Left(e)
              }
            }
          }

          loop(builder)
        }
      }
    }
}

private[codec] trait FamilyDecoder2 extends FamilyDecoder3 {

  implicit val decodeHNil: FamilyDecoder[HNil] = new FamilyDecoder[HNil] {
    def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, HNil] = Right(HNil)
  }

  implicit def decodeLabelledHCons[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: ValueCodec[H],
      T: FamilyDecoder[T]): FamilyDecoder[FieldType[K, H] :: T] =
    new FamilyDecoder[FieldType[K, H] :: T] {
      def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, FieldType[K, H] :: T] = {
        val k = map.get(Bytes.toBytes(K.value.name))
        H.decode(k) match {
          case Left(e) => Left(e)
          case Right(h) =>
            T(map) match {
              case Left(e)  => Left(e)
              case Right(t) => Right(field[K](h) :: t)
            }
        }
      }
    }

  implicit def decodeCaseClass[H <: HList, A](implicit
                                              gen: LabelledGeneric.Aux[A, H],
                                              A: Lazy[FamilyDecoder[H]]): FamilyDecoder[A] =
    new FamilyDecoder[A] {
      def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, A] =
        A.value(map) match {
          case Right(v) => Right(gen.from(v))
          case Left(e)  => Left(e)
        }
    }
}

private[codec] trait FamilyDecoder3 {

  implicit def decodeOption[A](
      implicit
      A: FamilyDecoder[A]): FamilyDecoder[Option[A]] =
    new FamilyDecoder[Option[A]] {
      def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, Option[A]] =
        if (map == null || map.isEmpty)
          Right(None)
        else
          A(map) match {
            case Right(v) => Right(Some(v))
            case Left(e)  => Left(e)
          }
    }
}
