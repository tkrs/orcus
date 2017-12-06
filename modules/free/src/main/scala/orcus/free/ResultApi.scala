package orcus.free

import java.nio.ByteBuffer

import cats.free.{Free, Inject}
import org.apache.hadoop.hbase.client.Result

trait ResultApi[F[_]] {
  type ResultF[A] = Free[F, A]

  def getValue(result: Result, family: Array[Byte], qualifier: Array[Byte]): ResultF[Array[Byte]]
  def getValueAsByteBuffer(result: Result,
                           family: Array[Byte],
                           qualifier: Array[Byte]): ResultF[ByteBuffer]
}

sealed trait ResultOp[A]

object ResultOp {
  final case class GetValue(result: Result, family: Array[Byte], qualifier: Array[Byte])
      extends ResultOp[Array[Byte]]
  final case class GetValueAsByteBuffer(result: Result, family: Array[Byte], qualifier: Array[Byte])
      extends ResultOp[ByteBuffer]
}

class ResultOps[M[_]](implicit inj: Inject[ResultOp, M]) extends ResultApi[M] {
  import ResultOp._

  override def getValue(result: Result,
                        family: Array[Byte],
                        qualifier: Array[Byte]): ResultF[Array[Byte]] =
    Free.inject[ResultOp, M](GetValue(result, family, qualifier))

  override def getValueAsByteBuffer(result: Result,
                                    family: Array[Byte],
                                    qualifier: Array[Byte]): ResultF[ByteBuffer] =
    Free.inject[ResultOp, M](GetValueAsByteBuffer(result, family, qualifier))
}

object ResultOps {
  implicit def resultOps[M[_]](implicit inj: Inject[ResultOp, M]): ResultOps[M] = new ResultOps
}
