package orcus.codec

import java.util.{NavigableMap => NMap}

import cats.Eval
import export.imports

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

object FamilyDecoder extends LowPriorityFamilyDecoder {

  @inline def apply[A](implicit A: FamilyDecoder[A]): FamilyDecoder[A] = A

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

@imports[FamilyDecoder]
trait LowPriorityFamilyDecoder
