package orcus

import java.nio.ByteBuffer

import cats.MonadError
import org.apache.hadoop.hbase.Cell
import org.apache.hadoop.hbase.client.Result

import scala.collection.JavaConverters._

object result {

  def getRow[M[_]](r: Result)(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Option[Array[Byte]]] =
    ME.pure(Option(r.getRow))

  def rawCells[M[_]](r: Result)(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Seq[Cell]] =
    ME.pure(r.rawCells() match { case null => Vector.empty; case xs => xs.toSeq })

  def getColumnCells[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Seq[Cell]] =
    ME.pure(r.getColumnCells(family, qualifier).asScala)

  def getColumnLatestCell[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Option[Cell]] =
    ME.pure(Option(r.getColumnLatestCell(family, qualifier)))

  def getValue[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Option[Array[Byte]]] =
    ME.pure(Option(r.getValue(family, qualifier)))

  def getValueAsByteBuffer[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Option[ByteBuffer]] =
    ME.pure(Option(r.getValueAsByteBuffer(family, qualifier)))

  def getFamilyMap[M[_]](r: Result, family: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Map[Array[Byte], Array[Byte]]] =
    ME.pure(r.getFamilyMap(family) match { case null => Map.empty; case xs => xs.asScala.toMap })
}
