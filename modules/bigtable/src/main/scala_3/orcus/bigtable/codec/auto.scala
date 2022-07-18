package orcus.bigtable.codec

// import shapeless3.deriving.*

object auto {
  given autoDerivedFamilyDecoder[A](using A: => DerivedFamilyDecoder[A]): FamilyDecoder[A] = A
  given autoDerivedRowDecoder[A](using A: => DerivedRowDecoder[A]): RowDecoder[A]          = A

  given autoDerivedFamilyEncoder[A](using A: => DerivedFamilyEncoder[A]): FamilyEncoder[A] = A
}
