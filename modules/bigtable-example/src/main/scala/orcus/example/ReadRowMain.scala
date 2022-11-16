package orcus.example

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all._
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest
import com.google.cloud.bigtable.admin.v2.models.GCRules
import com.google.cloud.bigtable.data.v2.BigtableDataClient
import com.google.cloud.bigtable.data.v2.models.Query
import com.google.cloud.bigtable.data.v2.models.RowMutation
import com.google.protobuf.ByteString
import com.typesafe.scalalogging.LazyLogging
import orcus.async.instances.catsEffect._
import orcus.bigtable.DataClient
import orcus.bigtable.async.implicits._

import scala.util.Random

object ReadRowMain extends IOApp with LazyLogging {
  import Settings._

  override def run(args: List[String]): IO[ExitCode] =
    createTable >> mutateAndRead >> IO(ExitCode.Success)

  private def tableRequest =
    CreateTableRequest.of("table").addFamily("c1").addFamily("c2", GCRules.GCRULES.maxVersions(4))

  private def createTable =
    IO(BigtableTableAdminClient.create(adminSettings))
      .flatTap(c => IO(logger.info(s"bigtable admin client created: $c")))
      .bracket(c =>
        IO(c.exists("table"))
          .flatTap(r => IO(logger.info(s"table exists: $r")))
          .ifM(IO.unit, IO(c.createTable(tableRequest)).flatTap(t => IO(logger.info(s"table created: $t"))))
      )(r => IO(r.close()))

  private def mutateAndRead =
    IO(BigtableDataClient.create(dataSettings))
      .flatTap(c => IO(logger.info(s"bigtable data client created: $c")))
      .bracket(r => runMutate(r).flatTap(_ => IO(logger.info("mutated"))) >> runRead(r))(r => IO(r.close()))
      .handleErrorWith { e =>
        e.printStackTrace(); IO.raiseError(e)
      }

  private def runMutate(dataClient: BigtableDataClient): IO[Unit] = {
    val wrapped = DataClient[IO](dataClient)

    val now = System.currentTimeMillis() * 1000L
    val rowMutation = RowMutation
      .create("table", "key")
      .setCell("c1", ByteString.copyFromUtf8("q1"), now, ByteString.copyFromUtf8(Random.alphanumeric.take(3).mkString))
      .setCell("c1", ByteString.copyFromUtf8("q2"), now, ByteString.copyFromUtf8(Random.alphanumeric.take(2).mkString))
      .setCell("c2", ByteString.copyFromUtf8("q1"), now, ByteString.copyFromUtf8(Random.alphanumeric.take(5).mkString))

    wrapped.mutateRowAsync(rowMutation) >> IO.unit
  }

  private def runRead(dataClient: BigtableDataClient): IO[Unit] =
    readRow(dataClient)
      .map { case (x, y) =>
        logger.info(s"got the row as Map: $x")
        logger.info(s"got the row as T: $y")
      }

  private def readRow(dataClient: BigtableDataClient): IO[(Map[String, Map[String, List[String]]], T)] = {
    logger.info("readRow start")
    val wrapped = DataClient[IO](dataClient)
    val query   = Query.create("table")

    val read = wrapped
      .readRowAsync(query)
      .flatTap(row =>
        IO {
          row.foreach { r =>
            logger.info(s"rowkey: ${r.rowKey}")
            r.families.foreach { case (k, v) =>
              logger.info(s"> family: $k")
              v.foreach(c =>
                logger.info(
                  s">> qualifier: ${c.getQualifier().toStringUtf8()}, value: ${c.getValue().toStringUtf8()}, timestamp: ${c.getTimestamp()}"
                )
              )
            }
          }
        }
      )

    import orcus.bigtable.codec.auto._
    (read >>= (IO.fromOption(_)(new RuntimeException("not found")))) >>= (c =>
      (IO.fromEither(c.decode[Map[String, Map[String, List[String]]]]), IO.fromEither(c.decode[T])).tupled
    )
  }
}
