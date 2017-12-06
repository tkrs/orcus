package orcus

import java.nio.ByteBuffer

import cats.MonadError
import cats.data.Kleisli
import org.apache.hadoop.hbase.client.Result

object result {

  def getValue[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[Array[Byte]] =
    ME.catchNonFatal(r.getValue(family, qualifier))

  def getValueAsByteBuffer[M[_]](r: Result, family: Array[Byte], qualifier: Array[Byte])(
      implicit
      ME: MonadError[M, Throwable]
  ): M[ByteBuffer] =
    ME.catchNonFatal(r.getValueAsByteBuffer(family, qualifier))
}
