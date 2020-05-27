package orcus.free.handler

import cats.{~>, ApplicativeError}
import cats.data.Kleisli
import orcus.async._
import orcus.async.implicits._
import orcus.free.TableOp
import orcus.table.AsyncTableT

object table {
  trait Handler[M[_]] extends (TableOp ~> Kleisli[M, AsyncTableT, ?])

  object Handler {
    import orcus.table._
    import TableOp._

    implicit def tableOpHandler[M[_]](implicit
      apErrorM: ApplicativeError[M, Throwable],
      asyncHandlerM: AsyncHandler[M]
    ): Handler[M] =
      new Handler[M] {
        def apply[A](fa: TableOp[A]): Kleisli[M, AsyncTableT, A] =
          fa match {
            case GetName          => kleisli(getName[M])
            case GetConfiguration => kleisli(getConfiguration[M])
            case Exists(a)        => kleisli(exists[M](_, a))
            case Get(a)           => kleisli(get[M](_, a))
            case Put(a)           => kleisli(put[M](_, a))
            case GetScanner(a)    => kleisli(getScanner[M](_, a))
            case ScanAll(a)       => kleisli(scanAll[M](_, a))
            case Delete(a)        => kleisli(delete[M](_, a))
            case Append(a)        => kleisli(append[M](_, a))
            case Increment(a)     => kleisli(increment[M](_, a))
            case batch @ Batch(_) => kleisli(batch.run[M])
          }
      }
  }
}
