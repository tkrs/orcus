package orcus.internal

import scala.jdk.CollectionConverters.*

object Utils {
  @inline def toSeq[A](list: java.util.List[A]): Seq[A] =
    list.asScala.toSeq
  @inline def toList[A](a: java.util.List[A]): List[A] =
    a.asScala.toList
  @inline def toIterator[A](list: java.util.List[A]): Iterator[A] =
    list.iterator.asScala
  @inline def toMap[K, V](a: java.util.Map[K, V]): Map[K, V] =
    a.asScala.toMap
  @inline def toJavaList[A](list: Seq[A]): java.util.List[A] =
    list.asJava
}
