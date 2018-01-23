package orcus

package object codec {

  implicit class ArrayOps private[orcus] (val a: Option[Array[Byte]]) extends AnyVal {
    def orEmpty: Array[Byte] = a.getOrElse(Array.emptyByteArray)
  }
}
