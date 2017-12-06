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
          case Name                     => nameK[M]
          case Configuration            => configurationK[M]
          case Descriptor               => descriptorK[M]
          case Exists(a)                => existsK[M](a)
          case Get(a: HGet)             => getK[M](a)
          case Put(a: HPut)             => putK[M](a)
          case Scan(a: HScan)           => scanK[M](a)
          case Delete(a: HDelete)       => deleteK[M](a)
          case Append(a: HAppend)       => appendK[M](a)
          case Increment(a: HIncrement) => incrementK[M](a)
          case Close                    => closeK[M]
        }
      }
  }
}
