package orcus

import cats.MonadError
import org.apache.hadoop.hbase.client.{Result, ResultScanner}

object resultScanner {
  def nextOne[F[_]](resultScanner: ResultScanner)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[Option[Result]] =
    ME.catchNonFatal(Option(resultScanner.next()))

  def next[F[_]](resultScanner: ResultScanner, i: Int)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[Seq[Result]] =
    ME.catchNonFatal(resultScanner.next(i).toSeq)
}
