package orcus.codec

import java.util
import java.util.function.BiConsumer

import cats.Eval
import org.apache.hadoop.hbase.util.Bytes
import shapeless.labelled._
import shapeless._

import scala.collection.generic.CanBuildFrom
import scala.util.control.NonFatal

trait FamilyDecoder[A] { self =>

  def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, A]

  def flatMap[B](f: A => FamilyDecoder[B]): FamilyDecoder[B] = new FamilyDecoder[B] {
    def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, B] = {
      self(map) match {
        case Right(a)    => f(a)(map)
        case l @ Left(_) => l.asInstanceOf[Either[Throwable, B]]
      }
    }
  }

  def map[B](f: A => B): FamilyDecoder[B] = new FamilyDecoder[B] {
    def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, B] =
      self(map) match {
        case Right(a)    => Right(f(a))
        case l @ Left(_) => l.asInstanceOf[Either[Throwable, B]]
      }
  }

  def mapF[B](f: A => Either[Throwable, B]): FamilyDecoder[B] = new FamilyDecoder[B] {
    def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, B] =
      self(map) match {
        case Right(a)    => f(a)
        case l @ Left(_) => l.asInstanceOf[Either[Throwable, B]]
      }
  }
}

object FamilyDecoder extends FamilyDecoder1 {

  def apply[A](implicit A: FamilyDecoder[A]): FamilyDecoder[A] = A

  def pure[A](a: A): FamilyDecoder[A] = new FamilyDecoder[A] {
    def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, A] =
      Right(a)
  }

  def eval[A](a: Eval[A]): FamilyDecoder[A] = new FamilyDecoder[A] {
    def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, A] =
      Right(a.value)
  }

  def liftF[A](a: Either[Throwable, A]): FamilyDecoder[A] = new FamilyDecoder[A] {
    def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, A] = a
  }
}

trait FamilyDecoder1 extends FamilyDecoder2 {

  implicit def decodeMap[M[_, _] <: Map[K, V], K, V](
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V],
      cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): FamilyDecoder[M[K, V]] =
    new FamilyDecoder[M[K, V]] {
      def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, M[K, V]] = {
        val m = cbf()
        if (map == null)
          Right(m.result())
        else {
          val f = new BiConsumer[Array[Byte], Array[Byte]] {
            def accept(t: Array[Byte], u: Array[Byte]): Unit = {
              val tt = Option(t).orEmpty
              val uu = Option(u).orEmpty
              (K.decode(tt), V.decode(uu)) match {
                case (Some(k), Some(v)) =>
                  m += k -> v
                case (_, _) =>
                  m
              }
              ()
            }
          }
          map.forEach(f)
          Right(m.result())
        }
      }
    }
}

trait FamilyDecoder2 extends FamilyDecoder3 {

  implicit def decodeHNil: FamilyDecoder[HNil] = new FamilyDecoder[HNil] {
    def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, HNil] =
      Right(HNil)
  }

  implicit def decodeLabelledHList[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: ValueCodec[H],
      T: Lazy[FamilyDecoder[T]]): FamilyDecoder[FieldType[K, H] :: T] =
    new FamilyDecoder[FieldType[K, H] :: T] {
      def apply(map: util.NavigableMap[Array[Byte], Array[Byte]])
        : Either[Throwable, FieldType[K, H] :: T] = {
        try {
          val h  = Option(map.get(Bytes.toBytes(K.value.name))).orEmpty
          val h0 = field[K](H.decode(h).getOrElse(null.asInstanceOf[H]))
          T.value(map) match {
            case Right(t) => Right(h0 :: t)
            case Left(e)  => Left(e)
          }
        } catch {
          case NonFatal(e) => Left(e)
        }
      }
    }

  implicit def decodeHCons[H <: HList, A0](implicit
                                           gen: LabelledGeneric.Aux[A0, H],
                                           A: Lazy[FamilyDecoder[H]]): FamilyDecoder[A0] =
    new FamilyDecoder[A0] {
      def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, A0] =
        A.value(map) match {
          case Right(v) => Right(gen.from(v))
          case Left(e)  => Left(e)
        }
    }
}

trait FamilyDecoder3 {

  implicit def decodeOption[A0](
      implicit
      A: Lazy[FamilyDecoder[A0]]): FamilyDecoder[Option[A0]] =
    new FamilyDecoder[Option[A0]] {
      def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, Option[A0]] =
        if (map == null || map.isEmpty)
          Right(None)
        else
          A.value(map) match {
            case Right(v) => Right(Some(v))
            case Left(e)  => Left(e)
          }
    }

}
