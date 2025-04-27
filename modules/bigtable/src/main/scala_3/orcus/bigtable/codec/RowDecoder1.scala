package orcus.bigtable.codec

private[codec] trait RowDecoder1:
  inline def derived[A](using A: DerivedRowDecoder[A]): DerivedRowDecoder[A] = A
