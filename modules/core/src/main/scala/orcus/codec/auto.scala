package orcus.codec

import shapeless.Lazy

object auto {
  implicit def autoDerivedDecoder[A](implicit A: Lazy[DerivedDecoder[A]]): Decoder[A]                   = A.value
  implicit def autoDerivedFamilyDecoder[A](implicit A: Lazy[DerivedFamilyDecoder[A]]): FamilyDecoder[A] = A.value

  implicit def autoDerivedPutEncoder[A](implicit A: Lazy[DerivedPutEncoder[A]]): PutEncoder[A] = A.value

  implicit def autoDerivedPutFamilyEncoder[A](implicit A: Lazy[DerivedPutFamilyEncoder[A]]): PutFamilyEncoder[A] =
    A.value
}
