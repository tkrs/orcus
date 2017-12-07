package orcus

import java.nio.ByteBuffer

import cats.MonadError
import org.apache.hadoop.hbase.client.Result

object result {

  def getValue[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Option[Array[Byte]]] =
    ME.catchNonFatal(Option(r.getValue(family, qualifier)))

  def getValueAsByteBuffer[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Option[ByteBuffer]] =
    ME.catchNonFatal(Option(r.getValueAsByteBuffer(family, qualifier)))
}
