package orcus.codec

import shapeless.Lazy

package object generic {

  def derivedDecoder[A](implicit A: Lazy[DerivedDecoder[A]]): DerivedDecoder[A] = A.value

  def derivedFamilyDecoder[A](implicit A: Lazy[DerivedFamilyDecoder[A]]): DerivedFamilyDecoder[A] = A.value

  def derivedPutEncoder[A](implicit A: Lazy[DerivedPutEncoder[A]]): DerivedPutEncoder[A] = A.value

  def derivedPutFamilyEncoder[A](implicit A: Lazy[DerivedPutFamilyEncoder[A]]): DerivedPutFamilyEncoder[A] = A.value
}
