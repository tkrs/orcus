package orcus.codec
package generic

import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import shapeless.labelled._
import shapeless._

trait DerivedDecoder[A] extends Decoder[A]

object DerivedDecoder extends DerivedDecoder1

private[codec] trait DerivedDecoder1 {

  implicit val decodeHNil: DerivedDecoder[HNil] = new DerivedDecoder[HNil] {
    def apply(result: Result): Either[Throwable, HNil] = Right(HNil)
  }

  implicit def decodeLabelledHCons[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: FamilyDecoder[H],
    T: DerivedDecoder[T]
  ): DerivedDecoder[FieldType[K, H] :: T] =
    new DerivedDecoder[FieldType[K, H] :: T] {
      def apply(result: Result): Either[Throwable, FieldType[K, H] :: T] =
        T(result) match {
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
    A: Lazy[DerivedDecoder[H]]
  ): DerivedDecoder[A] =
    new DerivedDecoder[A] {
      def apply(result: Result): Either[Throwable, A] =
        A.value(result) match {
          case Right(v) => Right(gen.from(v))
          case Left(e)  => Left(e)
        }
    }
}
