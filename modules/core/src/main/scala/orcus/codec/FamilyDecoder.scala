package orcus.codec

import shapeless.labelled._
import shapeless._

trait FamilyDecoder[A] {
  def apply(map: Map[String, Array[Byte]]): Either[Throwable, A]
}

object FamilyDecoder extends FamilyDecoder1 {

  def apply[A](implicit A: FamilyDecoder[A]): FamilyDecoder[A] = A
}

trait FamilyDecoder1 extends FamilyDecoder2 {

  implicit def decodeHNil: FamilyDecoder[HNil] = new FamilyDecoder[HNil] {
    def apply(map: Map[String, Array[Byte]]): Either[Throwable, HNil] = Right(HNil)
  }

  implicit def decodeLabelledHList[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: ValueCodec[H],
      T: Lazy[FamilyDecoder[T]]): FamilyDecoder[FieldType[K, H] :: T] =
    new FamilyDecoder[FieldType[K, H] :: T] {
      def apply(map: Map[String, Array[Byte]]): Either[Throwable, FieldType[K, H] :: T] = {
        val key = K.value.name
        map.get(key) match {
          case Some(v0) =>
            val h = field[K](H.decode(v0))
            T.value(map) match {
              case Right(t) => Right(h :: t)
              case Left(e)  => Left(e)
            }
          case None =>
            Left(new Exception(s"$key is not contains in map: $map"))
        }
      }
    }

  implicit def decodeHCons[H <: HList, A0](implicit
                                           gen: LabelledGeneric.Aux[A0, H],
                                           A: Lazy[FamilyDecoder[H]]): FamilyDecoder[A0] =
    new FamilyDecoder[A0] {
      def apply(map: Map[String, Array[Byte]]): Either[Throwable, A0] =
        A.value(map) match { case Right(v) => Right(gen.from(v)); case Left(e) => Left(e) }
    }
}

trait FamilyDecoder2 {

  implicit def decodeHConsOption[H <: HList, A0](
      implicit
      gen: LabelledGeneric.Aux[A0, H],
      A: Lazy[FamilyDecoder[H]]): FamilyDecoder[Option[A0]] =
    new FamilyDecoder[Option[A0]] {
      def apply(map: Map[String, Array[Byte]]): Either[Throwable, Option[A0]] =
        A.value(map) match {
          case Right(v) => Right(Some(gen.from(v)))
          case Left(_)  => Right(None)
        }
    }

}
