package orcus.bigtable.codec

import orcus.bigtable.Row

private[codec] trait RowDecoder1:
  inline def derived[A](using A: DerivedRowDecoder[A]): DerivedRowDecoder[A] = A
