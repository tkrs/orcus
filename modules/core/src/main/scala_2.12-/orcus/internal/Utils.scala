package orcus.internal

import scala.collection.JavaConverters._

object Utils {
  @inline def toSeq[A](a: java.util.List[A]): Seq[A] =
    a.asScala.toSeq
  @inline def toList[A](a: java.util.List[A]): List[A] =
    a.asScala.toList
  @inline def toIterator[A](a: java.util.List[A]): Iterator[A] =
    a.iterator.asScala
  @inline def toMap[K, V](a: java.util.Map[K, V]): Map[K, V] =
    a.asScala.toMap
  @inline def toJavaList[A](a: Seq[A]): java.util.List[A] =
    a.asJava
}
