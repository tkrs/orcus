package orcus.example

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

import cats.effect.ContextShift
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all._
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.ServiceOptions
import com.google.cloud.bigtable.data.v2.BigtableDataClient
import com.google.cloud.bigtable.data.v2.BigtableDataSettings
import com.google.cloud.bigtable.data.v2.models.Filters
import com.google.cloud.bigtable.data.v2.models.Query
import com.google.cloud.bigtable.data.v2.models.RowMutation
import com.google.common.primitives.Ints
import com.google.protobuf.ByteString
import com.typesafe.scalalogging.LazyLogging
import orcus.async.instances.catsEffect.concurrent._
import orcus.bigtable.CRow
import orcus.bigtable.DataClient
import orcus.bigtable.async.implicits._
import orcus.bigtable.codec.FamilyDecoder
import orcus.bigtable.codec.PrimitiveDecoder
import orcus.bigtable.codec.RowDecoder
import orcus.bigtable.codec.semiauto._

import scala.util.control.NonFatal

object Main extends IOApp with LazyLogging {
  private[this] val dataSettings = {
    if (sys.env.contains("BIGTABLE_EMULATOR_HOST"))
      BigtableDataSettings.newBuilder().setProjectId("fake").setInstanceId("fake").build()
    else {
      val builder = BigtableDataSettings
        .newBuilder()
        .setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.getApplicationDefault))
        .setProjectId(ServiceOptions.getDefaultProjectId)
        .setInstanceId(sys.env.getOrElse("BIGTABLE_INSTANCE", "fake"))
        .setAppProfileId(sys.env.getOrElse("BIGTABLE_APP_PROFILE", "default"))

      val stubSettings = builder.stubSettings()
      val readRowsRetrySettings = stubSettings
        .readRowsSettings()
        .getRetrySettings
        .toBuilder
        .setInitialRetryDelay(org.threeten.bp.Duration.ofMillis(5))
        .setRetryDelayMultiplier(2)
        .setMaxRetryDelay(org.threeten.bp.Duration.ofMillis(500L))
        .setInitialRpcTimeout(org.threeten.bp.Duration.ofSeconds(1L))
        .setRpcTimeoutMultiplier(1.0)
        .setMaxRpcTimeout(org.threeten.bp.Duration.ofSeconds(3L))
        .setTotalTimeout(org.threeten.bp.Duration.ofSeconds(5L))
        .build()
      val readRowRetrySettings = stubSettings
        .readRowSettings()
        .getRetrySettings
        .toBuilder
        .setInitialRetryDelay(org.threeten.bp.Duration.ofMillis(5))
        .setRetryDelayMultiplier(2)
        .setMaxRetryDelay(org.threeten.bp.Duration.ofMillis(500L))
        .setInitialRpcTimeout(org.threeten.bp.Duration.ofSeconds(1L))
        .setRpcTimeoutMultiplier(1.0)
        .setMaxRpcTimeout(org.threeten.bp.Duration.ofSeconds(2L))
        .setTotalTimeout(org.threeten.bp.Duration.ofSeconds(5L))
        .build()
      val mutateRowRetrySettings = stubSettings
        .mutateRowSettings()
        .getRetrySettings
        .toBuilder
        .setInitialRetryDelay(org.threeten.bp.Duration.ofMillis(5))
        .setRetryDelayMultiplier(2)
        .setMaxRetryDelay(org.threeten.bp.Duration.ofMillis(500L))
        .setInitialRpcTimeout(org.threeten.bp.Duration.ofSeconds(1L))
        .setRpcTimeoutMultiplier(1.0)
        .setMaxRpcTimeout(org.threeten.bp.Duration.ofSeconds(1L))
        .setTotalTimeout(org.threeten.bp.Duration.ofSeconds(5L))
        .build()
      stubSettings.readRowsSettings().setRetrySettings(readRowsRetrySettings)
      stubSettings.readRowSettings().setRetrySettings(readRowRetrySettings)
      stubSettings.mutateRowSettings().setRetrySettings(mutateRowRetrySettings)

      builder.build()
    }
  }

  private[this] val tableId = "cpu"

  object family {
    val metric = "metric"
  }

  object qualifiers {
    val percentage = ByteString.copyFromUtf8("percentage")
    val tags       = ByteString.copyFromUtf8("tags")
  }

  private[this] val userName = sys.env("USER")
  private[this] val keySep   = "#"

  private[this] val cpuNums = (1 to sys.runtime.availableProcessors()).toList

  override def run(args: List[String]): IO[ExitCode] =
    IO(BigtableDataClient.create(dataSettings))
      .bracket(r => runMutate(r) >> runRead(r))(r => IO(r.close())) >> IO(ExitCode.Success)

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
        .create(tableId, Seq(userName, num.toString, reversedCurrentMillis.toString).mkString(keySep))
        .setCell(family.metric, qualifiers.percentage, micros, ByteString.copyFrom(Ints.toByteArray(usage)))
        .setCell(family.metric, qualifiers.tags, micros, ByteString.copyFromUtf8(tags.mkString(",")))

      wrapped.mutateRowAsync(rowMutation)
    }
    (traverse <* ContextShift[IO].shift) >> IO.unit
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
    val query  = Query.create(tableId).prefix(userName + keySep).filter(filter)

    ((wrapped.readRowsAsync(query) <* ContextShift[IO].shift) >>=
      (rows => IO.fromEither(CRow.decodeRows[(String, CPU)](rows))))
      .flatTap(a => IO(logger.info(s"readRows: ${a.toString}")))
  }
}

final case class CPU(metric: Metric)

object CPU {
  implicit val decode: RowDecoder[CPU] = derivedRowDecoder[CPU]
}

final case class Metric(percentage: Int, tags: List[String])

object Metric {
  implicit val decodeTags: PrimitiveDecoder[List[String]] = bs =>
    try if (bs == null) Right(Nil)
    else Right(bs.toStringUtf8.split(",").toList.map(_.trim))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decode: FamilyDecoder[Metric] = derivedFamilyDecoder[Metric]
}
