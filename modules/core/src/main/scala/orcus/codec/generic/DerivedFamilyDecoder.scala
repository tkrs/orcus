package orcus.codec
package generic

import java.util.{NavigableMap => NMap}

import export.exports
import org.apache.hadoop.hbase.util.Bytes
import shapeless.labelled._
import shapeless._

trait DerivedFamilyDecoder[A] extends FamilyDecoder[A]

@exports
object DerivedFamilyDecoder extends DerivedFamilyDecoder1

private[codec] trait DerivedFamilyDecoder1 {

  implicit val familyDecodeHNil: DerivedFamilyDecoder[HNil] = new DerivedFamilyDecoder[HNil] {
    def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, HNil] = Right(HNil)
  }

  implicit def familyDecodeLabelledHCons[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: ValueCodec[H],
      T: DerivedFamilyDecoder[T]): DerivedFamilyDecoder[FieldType[K, H] :: T] =
    new DerivedFamilyDecoder[FieldType[K, H] :: T] {
      def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, FieldType[K, H] :: T] =
        T(map) match {
          case Right(t) =>
            H.decode(map.get(Bytes.toBytes(K.value.name))) match {
              case Right(h) => Right(field[K](h) :: t)
              case Left(e)  => Left(e)
            }
          case Left(e) => Left(e)
        }
    }

  implicit def familyDecodeLabelledGen[H <: HList, A](implicit
                                                      gen: LabelledGeneric.Aux[A, H],
                                                      A: Lazy[DerivedFamilyDecoder[H]]): DerivedFamilyDecoder[A] =
    new DerivedFamilyDecoder[A] {
      def apply(map: NMap[Array[Byte], Array[Byte]]): Either[Throwable, A] =
        A.value(map) match {
          case Right(v) => Right(gen.from(v))
          case Left(e)  => Left(e)
        }
    }
}
