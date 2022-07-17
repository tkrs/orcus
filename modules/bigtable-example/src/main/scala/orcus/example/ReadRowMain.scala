package orcus.example

import cats.effect.ContextShift
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
import orcus.async.instances.catsEffect.concurrent._
import orcus.bigtable.DataClient
import orcus.bigtable.async.implicits._

object ReadRowMain extends IOApp with LazyLogging {
  import Settings._

  override def run(args: List[String]): IO[ExitCode] =
    createTable >> mutateAndRead >> IO(ExitCode.Success)

  private def tableRequest =
    CreateTableRequest.of("table").addFamily("c1").addFamily("c2", GCRules.GCRULES.maxVersions(4))

  private def createTable =
    IO(BigtableTableAdminClient.create(adminSettings))
      .bracket(c => IO(c.exists("table")).ifM(IO.unit, IO(c.createTable(tableRequest))))(r => IO(r.close()))

  private def mutateAndRead =
    IO(BigtableDataClient.create(dataSettings))
      .bracket(r => runMutate(r) >> runRead(r))(r => IO(r.close()))

  private def runMutate(dataClient: BigtableDataClient): IO[Unit] = {
    val wrapped = DataClient[IO](dataClient)

    val now = System.currentTimeMillis() * 1000L
    val rowMutation = RowMutation
      .create("table", "key")
      .setCell("c1", ByteString.copyFromUtf8("q1"), now, ByteString.copyFromUtf8("aa"))
      .setCell("c1", ByteString.copyFromUtf8("q2"), now, ByteString.copyFromUtf8("bb"))
      .setCell("c2", ByteString.copyFromUtf8("q1"), now, ByteString.copyFromUtf8("cc"))

    (wrapped.mutateRowAsync(rowMutation) <* ContextShift[IO].shift) >> IO.unit
  }

  private def runRead(dataClient: BigtableDataClient): IO[Unit] =
    readRow(dataClient)
      .map(v => logger.info(s"got the row: $v"))

  private def readRow(dataClient: BigtableDataClient): IO[Map[String, Map[String, List[String]]]] = {
    logger.info("readRow start")
    val wrapped = DataClient[IO](dataClient)
    val query   = Query.create("table")

    val read = (wrapped.readRowAsync(query) <* ContextShift[IO].shift)
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

    (read >>= (IO.fromOption(_)(new RuntimeException("not found")))) >>=
      (c => IO.fromEither(c.decode[Map[String, Map[String, List[String]]]]))
  }
}
