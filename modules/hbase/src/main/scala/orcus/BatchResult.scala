package orcus

import org.apache.hadoop.hbase.client.Row
import org.apache.hadoop.hbase.client.{Result => HResult}

sealed trait BatchResult

object BatchResult {
  final case class Error(t: Throwable, action: Row) extends BatchResult {
    override def equals(obj: scala.Any): Boolean =
      obj match {
        case Error(tt, act) => t.getMessage == tt.getMessage && act == action
        case _              => false
      }
  }

  final case class Mutate(r: Option[HResult]) extends BatchResult

  final case object VoidMutate extends BatchResult
}
