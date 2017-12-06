package example.bigtable

import java.io.Closeable
import java.time.Instant

import cats.data.Kleisli
import cats.~>
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.TableName

import scala.collection.JavaConverters._
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object Functions {
  val tableName: TableName          = TableName.valueOf("Hello")
  val columnFamilyName: Array[Byte] = Bytes.toBytes("cf1")
  val columnName: Array[Byte]       = Bytes.toBytes("greeting1")
  val greeting: String              = "Hello World!"

  def bracket[R <: Closeable, A](tr: Try[R])(f: R => Try[A]): Try[A] = tr match {
    case Failure(e) =>
      Failure(e)
    case Success(r) =>
      try {
        f(r).map { a =>
          try r.close()
          catch { case NonFatal(_) => () }
          a
        }
      } catch {
        case NonFatal(e) =>
          try r.close()
          catch { case NonFatal(_) => () }
          Failure(e)
      }
  }

  def getAdmin(c: Connection): Try[Admin] = Try(c.getAdmin)

//  def getTable(c: Connection): Try[Table] = Try(c.getTable(tableName))
//
//  def runPut(table: Table, maxPuts: Int = 10): Try[Vector[Long]] = Try {
//    def go(ts: Long, acc: Vector[Long]): Vector[Long] =
//      if (acc.size > maxPuts) acc
//      else {
//        val rowKey = Bytes.toBytes(s"greeting#${Long.MaxValue - ts}")
//        val put    = new Put(rowKey, ts)
//        put.setTTL(1800)
//        put.addColumn(columnFamilyName,
//                      columnName,
//                      Bytes.toBytes(s"$greeting at ${Instant.ofEpochMilli(ts)}"))
//        table.put(put)
//        Thread.sleep(1)
//        go(System.currentTimeMillis(), acc :+ ts)
//      }
//
//    go(System.currentTimeMillis(), Vector.empty)
//  }
//
//  def runGet(table: Table, versions: Vector[Long]): Try[Unit] = Try {
//    val rowKey = s"greeting#${Long.MaxValue - versions.head}"
//    val get    = new Get(Bytes.toBytes(rowKey))
//    val result = table.get(get)
//    val value  = Option(result.getValue(columnFamilyName, columnName)).map(Bytes.toString)
//    println(s"Get row by $rowKey from $tableName, value: $value")
//  }
//
//  def runScan(table: Table, versions: Vector[Long]): Try[Unit] = Try {
//    val rowKey = s"greeting"
//    val scan   = new Scan()
//    scan.setRowPrefixFilter(Bytes.toBytes(rowKey))
//    scan.setTimeRange(versions.head, versions.tail.tail.tail.head)
//    // scan.setLoadColumnFamiliesOnDemand(true)
//    val resultScanner = table.getScanner(scan)
//    val results       = resultScanner.next(10).toSeq
//    results.foreach { result =>
//      val value = result.getValue(columnFamilyName, columnName)
//      val vm    = result.getNoVersionMap
//      println(s"Scan row from $rowKey, value: ${Bytes
//        .toString(value)}, versions: ${vm.keySet().asScala.map(Bytes.toString)}")
//    }
//  }

  implicit final class Nat[F[_], G[_]](val nat: F ~> G) extends AnyVal {
    def liftF[E]: F ~> Kleisli[G, E, ?] = λ[F ~> Kleisli[G, E, ?]](fa => Kleisli(_ => nat(fa)))
  }
}
