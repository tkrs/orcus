package orcus

import java.nio.ByteBuffer

import cats.{Monad, MonadError}
import codec.{Decoder, FamilyDecoder, ValueCodec}
import org.apache.hadoop.hbase.Cell
import org.apache.hadoop.hbase.client.Result

import scala.collection.JavaConverters._

object result {

  def getRow[M[_]](r: Result)(
      implicit
      M: Monad[M]
  ): M[Option[Array[Byte]]] =
    M.pure(Option(r.getRow))

  def rawCells[M[_]](r: Result)(
      implicit
      M: Monad[M]
  ): M[Seq[Cell]] =
    M.pure(r.rawCells() match { case null => Vector.empty; case xs => xs.toSeq })

  def getColumnCells[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      M: Monad[M]
  ): M[Seq[Cell]] =
    M.pure(r.getColumnCells(family, qualifier).asScala)

  def getColumnLatestCell[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      M: Monad[M]
  ): M[Option[Cell]] =
    M.pure(Option(r.getColumnLatestCell(family, qualifier)))

  def get[A, M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      A: ValueCodec[A],
      M: Monad[M]
  ): M[Option[A]] =
    M.map(getValue[M](r, family, qualifier))(_.map(A.decode))

  def getValue[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      M: Monad[M]
  ): M[Option[Array[Byte]]] =
    M.pure(Option(r.getValue(family, qualifier)))

  def getValueAsByteBuffer[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      M: Monad[M]
  ): M[Option[ByteBuffer]] =
    M.pure(Option(r.getValueAsByteBuffer(family, qualifier)))

  def getFamily[A, M[_]](r: Result, family: Array[Byte])(
      implicit
      A: FamilyDecoder[A],
      ME: MonadError[M, Throwable]
  ): M[A] =
    A(r.getFamilyMap(family)) match {
      case Right(v) => ME.pure(v)
      case Left(e)  => ME.raiseError(e)
    }

  def getFamilyMap[M[_]](r: Result, family: Array[Byte])(
      implicit
      ME: Monad[M]
  ): M[Map[Array[Byte], Array[Byte]]] =
    ME.pure(r.getFamilyMap(family) match {
      case null => Map.empty
      case xs   => xs.asScala.toMap
    })

  def to[A, M[_]](r: Result)(
      implicit
      A: Decoder[A],
      ME: MonadError[M, Throwable]
  ): M[A] =
    A.apply(r) match {
      case Right(a) => ME.pure(a)
      case Left(e)  => ME.raiseError(e)
    }
}
