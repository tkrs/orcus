package orcus.codec

import java.{util => ju}

import cats.{Eval, Foldable}
import cats.instances.either._
import cats.syntax.foldable._
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import shapeless.labelled._
import shapeless._

import scala.annotation.tailrec
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

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

  private[this] implicit val foldJavaSet: Foldable[ju.Set] = new Foldable[ju.Set] {
    def foldLeft[A, B](fa: ju.Set[A], b: B)(f: (B, A) => B): B = {
      @tailrec def loop(iter: ju.Iterator[A], acc: B): B = {
        if (!iter.hasNext)
          acc
        else
          loop(iter, f(acc, iter.next()))
      }
      loop(fa.iterator(), b)
    }

    def foldRight[A, B](fa: ju.Set[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = {
      def loop(iter: ju.Iterator[A]): Eval[B] = {
        if (!iter.hasNext)
          lb
        else
          f(iter.next(), Eval.defer(loop(iter)))
      }
      loop(fa.iterator())
    }
  }

  implicit def decodeMap[M[_, _] <: Map[String, V], V](
      implicit
      V: FamilyDecoder[V],
      cbf: CanBuildFrom[Nothing, (String, V), M[String, V]]): Decoder[M[String, V]] =
    new Decoder[M[String, V]] {

      type Out = mutable.Builder[(String, V), M[String, V]]

      def apply(result: Result): Either[Throwable, M[String, V]] = {
        val builder = cbf.apply
        val map     = result.getMap
        if (map == null) Right(builder.result())
        else {
          val xs = map.keySet()
          val ys = xs.foldLeftM[Either[Throwable, ?], Out](builder) {
            case (acc, cf) =>
              val cf0 = Bytes.toString(cf)
              V.apply(result.getFamilyMap(cf)) match {
                case Right(v) =>
                  Right(acc += cf0 -> v)
                case Left(e) =>
                  Left(e)
              }
          }
          ys match {
            case Right(b) => Right(b.result())
            case Left(e)  => Left(e)
          }
        }
      }
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
      T: Decoder[T]): Decoder[FieldType[K, H] :: T] =
    new Decoder[FieldType[K, H] :: T] {
      def apply(result: Result): Either[Throwable, FieldType[K, H] :: T] = {
        val k = Bytes.toBytes(K.value.name)
        val h = orcus.result.getFamily[H, Either[Throwable, ?]](result, k)
        h match {
          case Right(v0) =>
            T(result) match {
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
