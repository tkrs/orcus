package orcus.example

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all.*
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest
import com.google.cloud.bigtable.data.v2.BigtableDataClient
import com.google.cloud.bigtable.data.v2.models.Filters
import com.google.cloud.bigtable.data.v2.models.Query
import com.google.cloud.bigtable.data.v2.models.RowMutation
import com.google.cloud.bigtable.data.v2.models.TableId
import com.google.common.primitives.Ints
import com.google.protobuf.ByteString
import com.typesafe.scalalogging.LazyLogging
import orcus.async.instances.catsEffect.*
import orcus.bigtable.DataClient
import orcus.bigtable.Row
import orcus.bigtable.async.implicits.*
import orcus.bigtable.codec.FamilyDecoder
import orcus.bigtable.codec.PrimitiveDecoder
import orcus.bigtable.codec.RowDecoder
import orcus.bigtable.codec.semiauto.*

import scala.util.control.NonFatal

object ReadRowsMain extends IOApp with LazyLogging {
  import Settings._

  private val tableId = "cpu"

  object family {
    val metric = "metric"
  }

  object qualifiers {
    val percentage = ByteString.copyFromUtf8("percentage")
    val tags       = ByteString.copyFromUtf8("tags")
  }

  private val userName = sys.env("USER")
  private val keySep   = "#"

  private val cpuNums = (1 to sys.runtime.availableProcessors()).toList

  override def run(args: List[String]): IO[ExitCode] =
    createTable >> mutateAndRead >> IO(ExitCode.Success)

  private def tableRequest = CreateTableRequest.of(tableId).addFamily("metric")

  private def createTable =
    IO(BigtableTableAdminClient.create(adminSettings))
      .bracket(c => IO(c.exists(tableId)).ifM(IO.unit, IO(c.createTable(tableRequest))))(r => IO(r.close()))

  private def mutateAndRead =
    IO(BigtableDataClient.create(dataSettings))
      .bracket(r => runMutate(r) >> runRead(r))(r => IO(r.close()))

  private def runMutate(dataClient: BigtableDataClient): IO[Unit] = {
    val wrapped = DataClient[IO](dataClient)

    val millis                = System.currentTimeMillis()
    val micros                = millis * 1000L
    val reversedCurrentMillis = Long.MaxValue - millis
    val traverse = cpuNums.traverse[IO, Unit] { num =>
      logger.info(s"runMutate: $num")
      val usage = ThreadLocalRandom.current().nextInt(0, 100)
      val tags  = Seq("app:fake,location=asia")

      val rowMutation = RowMutation
        .create(TableId.of(tableId), Seq(userName, num.toString, reversedCurrentMillis.toString).mkString(keySep))
        .setCell(family.metric, qualifiers.percentage, micros, ByteString.copyFrom(Ints.toByteArray(usage)))
        .setCell(family.metric, qualifiers.tags, micros, ByteString.copyFromUtf8(tags.mkString(",")))

      wrapped.mutateRowAsync(rowMutation)
    }
    traverse >> IO.unit
  }

  private def runRead(dataClient: BigtableDataClient): IO[Unit] =
    readRows(dataClient)
      .map(_.map(_.toString))
      .map(_.foreach(v => logger.info(s"runRead: $v")))

  private def readRows(dataClient: BigtableDataClient): IO[Vector[(String, CPU)]] = {
    logger.info("readRows start")
    val wrapped = DataClient[IO](dataClient)

    val now    = Instant.now
    val start  = now.minus(Duration.ofMinutes(3)).toEpochMilli * 1000L
    val end    = now.toEpochMilli * 1000L
    val filter = Filters.FILTERS.timestamp().range().of(start, end)
    val query  = Query.create(TableId.of(tableId)).prefix(userName + keySep).filter(filter)

    val read =
      wrapped
        .readRowsAsync(query)
        .flatTap(rows =>
          IO(
            rows.zipWithIndex.foreach { case (r, i) =>
              logger.info(s"readRows[$i], rowkey: ${r.rowKey}, ${r.families.map { case (k, v) =>
                  k -> v.map(c => (c.getFamily(), c.getQualifier().toStringUtf8(), c.getTimestamp(), c.getValue().toStringUtf8()))
                }}")
            }
          )
        )

    val decode =
      (rows: Vector[Row]) =>
        IO.fromEither(Row.decodeRows[(String, CPU)](rows))
          .flatTap(a => IO(logger.info(s"decodeRows: ${a.toString}")))

    read >>= decode
  }
}

final case class CPU(metric: Metric)

object CPU {
  implicit val decode: RowDecoder[CPU] = derivedRowDecoder[CPU]
}

final case class Metric(percentage: Int, tags: List[Tags])

object Metric {
  implicit val decodeTags: PrimitiveDecoder[Tags] = bs =>
    try
      if (bs == null) Right(Tags.empty)
      else Right(Tags(bs.toStringUtf8.split(",").toList.map(_.trim)))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decode: FamilyDecoder[Metric] = derivedFamilyDecoder[Metric]
}

final case class Tags(value: List[String])
object Tags {
  val empty = Tags(Nil)
}
