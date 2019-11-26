package orcus

import cats.ApplicativeError
import org.apache.hadoop.hbase.client.{Result, ResultScanner}

object resultScanner {

  def nextOne[F[_]](resultScanner: ResultScanner)(
    implicit
    ME: ApplicativeError[F, Throwable]
  ): F[Option[Result]] =
    ME.catchNonFatal(Option(resultScanner.next()))

  def next[F[_]](resultScanner: ResultScanner, i: Int)(
    implicit
    ME: ApplicativeError[F, Throwable]
  ): F[Seq[Result]] =
    ME.catchNonFatal(resultScanner.next(i).toSeq)
}
