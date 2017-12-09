package orcus

import java.nio.ByteBuffer
import java.util.function.BiConsumer

import cats.MonadError
import codec.{Decoder, FamilyDecoder, ValueCodec}
import org.apache.hadoop.hbase.Cell
import org.apache.hadoop.hbase.client.Result

import scala.collection.JavaConverters._
import scala.collection.mutable

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

  def get[A, M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      A: ValueCodec[A],
      ME: MonadError[M, Throwable]
  ): M[Option[A]] =
    ME.map(getValue[M](r, family, qualifier))(_.map(A.decode))

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

  def getFamily[A, M[_]](r: Result, family: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable],
      A: FamilyDecoder[A]
  ): M[A] =
    ME.flatMap(getFamilyMap1[String, Array[Byte], M](r, family)) { m =>
      A(m) match {
        case Right(v) => ME.pure(v)
        case Left(e)  => ME.raiseError(e)
      }
    }

  def getFamilyMap[M[_]](r: Result, family: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Map[Array[Byte], Array[Byte]]] =
    ME.pure(r.getFamilyMap(family) match { case null => Map.empty; case xs => xs.asScala.toMap })

  def getFamilyMap1[K, V, M[_]](r: Result, family: Array[Byte])(
      implicit
      K: ValueCodec[K],
      V: ValueCodec[V],
      ME: MonadError[M, Throwable]
  ): M[Map[K, V]] =
    ME.pure(r.getFamilyMap(family) match {
      case null => Map.empty
      case xs =>
        val m = mutable.Map[K, V]()
        val f = new BiConsumer[Array[Byte], Array[Byte]] {
          override def accept(t: Array[Byte], u: Array[Byte]): Unit = {
            m += K.decode(t) -> V.decode(u)
            ()
          }
        }
        xs.forEach(f)
        m.toMap
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
