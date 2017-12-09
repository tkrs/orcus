package orcus.codec

import java.util
import java.util.function.BiConsumer

import org.apache.hadoop.hbase.util.Bytes
import shapeless.labelled._
import shapeless._

import scala.collection.generic.CanBuildFrom

trait FamilyDecoder[A] {
  def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, A]
}

object FamilyDecoder extends FamilyDecoder1 {

  def apply[A](implicit A: FamilyDecoder[A]): FamilyDecoder[A] = A

  implicit def decodeMap[M[_, _] <: Map[K, V], K, V](
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V],
      cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): FamilyDecoder[M[K, V]] =
    new FamilyDecoder[M[K, V]] {
      def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, M[K, V]] = {
        if (map == null)
          Left(new Exception(s"map is null"))
        else {
          val m = cbf()

          val f = new BiConsumer[Array[Byte], Array[Byte]] {
            override def accept(t: Array[Byte], u: Array[Byte]): Unit = {
              val k = K.decode(t)
              val v = V.decode(u)
              m += k -> v
              ()
            }
          }

          map.forEach(f)
          Right(m.result())
        }
      }
    }
}

trait FamilyDecoder1 extends FamilyDecoder2 {

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
        val key = K.value.name
        if (map == null)
          Left(new Exception(s"map is null"))
        else {
          map.get(Bytes.toBytes(key)) match {
            case null =>
              Left(new Exception(s"$key is not contains in map: $map"))
            case v0 =>
              val h = field[K](H.decode(v0))
              T.value(map) match {
                case Right(t) => Right(h :: t)
                case Left(e)  => Left(e)
              }
          }
        }
      }
    }

  implicit def decodeHCons[H <: HList, A0](implicit
                                           gen: LabelledGeneric.Aux[A0, H],
                                           A: Lazy[FamilyDecoder[H]]): FamilyDecoder[A0] =
    new FamilyDecoder[A0] {
      def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, A0] =
        A.value(map) match { case Right(v) => Right(gen.from(v)); case Left(e) => Left(e) }
    }
}

trait FamilyDecoder2 {

  implicit def decodeHConsOption[A0](
      implicit
      A: Lazy[FamilyDecoder[A0]]): FamilyDecoder[Option[A0]] =
    new FamilyDecoder[Option[A0]] {
      def apply(map: util.NavigableMap[Array[Byte], Array[Byte]]): Either[Throwable, Option[A0]] =
        A.value(map) match {
          case Right(v) => Right(Some(v))
          case Left(_)  => Right(None)
        }
    }

}
