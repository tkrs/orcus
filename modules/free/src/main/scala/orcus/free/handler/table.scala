package orcus.free.handler

import cats.{MonadError, ~>}
import cats.data.Kleisli
import orcus.free.TableOp
import org.apache.hadoop.hbase.client.{Table => HTable}

object table {

  trait Handler[M[_]] extends (TableOp ~> Kleisli[M, HTable, ?])

  object Handler {
    import orcus.table._
    import TableOp._

    implicit def tableOpHandler[M[_]](
        implicit
        ME: MonadError[M, Throwable]): Handler[M] =
      new Handler[M] {
        override def apply[A](fa: TableOp[A]): Kleisli[M, HTable, A] = fa match {
          case GetName          => kleisli(getName[M])
          case GetConfiguration => kleisli(getConfiguration[M])
          case GetDescriptor    => kleisli(getDescriptor[M])
          case Exists(a)        => kleisli(exists[M](_, a))
          case Get(a)           => kleisli(get[M](_, a))
          case Put(a)           => kleisli(put[M](_, a))
          case Scan(a)          => kleisli(getScanner[M](_, a))
          case Delete(a)        => kleisli(delete[M](_, a))
          case Append(a)        => kleisli(append[M](_, a))
          case Increment(a)     => kleisli(increment[M](_, a))
          case Close            => kleisli(close[M])
        }
      }
  }
}
