package orcus.internal

private[orcus] object ScalaVersionSpecifics {
  private[orcus] type Factory[-E, +T] = scala.collection.Factory[E, T]
}
