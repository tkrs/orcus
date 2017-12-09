package orcus.codec

import cats.instances.either._
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import shapeless.labelled._
import shapeless._

trait Decoder[A] {
  def apply(result: Result): Either[Throwable, A]
}

object Decoder extends Decoder1 {

  def apply[A](implicit A: Decoder[A]): Decoder[A] = A
}

trait Decoder1 {

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
        type M[A] = Either[Throwable, A]
        orcus.result.getFamily[H, M](result, Bytes.toBytes(K.value.name)) match {
          case Right(v0) =>
            val h = field[K](v0)
            T.value(result) match {
              case Right(t) => Right(h :: t)
              case Left(e)  => Left(e)
            }
          case Left(e) =>
            Left(new Exception(s"${K.value.name} is not contains in $result", e))
        }
      }
    }

  implicit def decodeHCons[H <: HList, A0](implicit
                                           gen: LabelledGeneric.Aux[A0, H],
                                           A: Lazy[Decoder[H]]): Decoder[A0] =
    new Decoder[A0] {
      def apply(result: Result): Either[Throwable, A0] =
        A.value(result) match { case Right(v) => Right(gen.from(v)); case Left(e) => Left(e) }
    }
}
