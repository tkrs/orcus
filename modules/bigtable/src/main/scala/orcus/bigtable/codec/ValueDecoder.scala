package orcus.bigtable.codec

import cats.syntax.traverse._
import com.google.cloud.bigtable.data.v2.models.RowCell

trait ValueDecoder[A] {
  def apply(versions: List[RowCell]): Either[Throwable, A]
}

object ValueDecoder extends ValueDecoder1 {
  @inline def apply[A](implicit A: ValueDecoder[A]): ValueDecoder[A] = A
}

trait ValueDecoder1 extends ValueDecoder2 {

  private def decode[A: PrimitiveDecoder](versions: List[RowCell]): Either[Throwable, List[A]] =
    versions.traverse(v => PrimitiveDecoder[A].apply(v.getValue()))

  implicit def decodeVersions[A: PrimitiveDecoder]: ValueDecoder[List[A]] = versions => decode(versions)

  implicit def decodeVersionsAsVector[A: PrimitiveDecoder]: ValueDecoder[Vector[A]] = versions =>
    decode(versions).map(_.toVector)

  implicit def decodeVersionsAsSeq[A: PrimitiveDecoder]: ValueDecoder[Seq[A]] = versions => decode(versions).map(_.toSeq)

  implicit def decodeLatestAsOption[A: PrimitiveDecoder]: ValueDecoder[Option[A]] = versions =>
    if (versions == null || versions.isEmpty) Right(None)
    else PrimitiveDecoder[Option[A]].apply(versions.head.getValue())
}

trait ValueDecoder2 {

  implicit def decodeLatest[A: PrimitiveDecoder]: ValueDecoder[A] = versions =>
    PrimitiveDecoder[A].apply(versions.head.getValue())
}
