package orcus.free.handler

import cats.{MonadError, ~>}
import cats.data.Kleisli
import orcus.free.TableOp
import org.apache.hadoop.hbase.client.{
  Append => HAppend,
  Delete => HDelete,
  Get => HGet,
  Increment => HIncrement,
  Put => HPut,
  Scan => HScan,
  Table => HTable
}

object table {

  trait Handler[M[_]] extends (TableOp ~> Kleisli[M, HTable, ?])

  object Handler {
    import orcus.table._
    import TableOp._

    implicit def tableOpHandler[M[_], J](
        implicit
        ME: MonadError[M, Throwable]): Handler[M] =
      new Handler[M] {
        override def apply[A](fa: TableOp[A]): Kleisli[M, HTable, A] = fa match {
          case Name                     => kleisli(getName[M])
          case Configuration            => kleisli(getConfiguration[M])
          case Descriptor               => kleisli(getTableDescriptor[M])
          case Exists(a)                => kleisli(exists[M](_, a))
          case Get(a: HGet)             => kleisli(get[M](_, a))
          case Put(a: HPut)             => kleisli(put[M](_, a))
          case Scan(a: HScan)           => kleisli(getScanner[M](_, a))
          case Delete(a: HDelete)       => kleisli(delete[M](_, a))
          case Append(a: HAppend)       => kleisli(append[M](_, a))
          case Increment(a: HIncrement) => kleisli(increment[M](_, a))
          case Close                    => kleisli(close[M])
        }
      }
  }
}
