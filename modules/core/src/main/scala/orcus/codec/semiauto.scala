package orcus.codec

import shapeless.Lazy

object semiauto {
  def derivedDecoder[A](implicit A: Lazy[DerivedDecoder[A]]): Decoder[A]                   = A.value
  def derivedFamilyDecoder[A](implicit A: Lazy[DerivedFamilyDecoder[A]]): FamilyDecoder[A] = A.value

  def derivedPutEncoder[A](implicit A: Lazy[DerivedPutEncoder[A]]): PutEncoder[A] = A.value
}
