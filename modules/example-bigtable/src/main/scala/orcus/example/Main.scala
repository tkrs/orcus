package orcus.example

import java.time.{Duration, Instant}
import java.util.concurrent.ThreadLocalRandom

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.ServiceOptions
import com.google.cloud.bigtable.data.v2.models.{Filters, Query, RowMutation}
import com.google.cloud.bigtable.data.v2.{BigtableDataClient, BigtableDataSettings}
import com.google.protobuf.ByteString
import orcus.async.catsEffect.effect._
import orcus.bigtable.async.implicits._
import orcus.bigtable.{BigtableDataClientWrapper, PrimitiveDecoder}
import org.apache.hadoop.hbase.util.Bytes

import scala.util.control.NonFatal

object Main extends IOApp {

  private[this] val dataSettings = {
    if (sys.env.contains("BIGTABLE_EMULATOR_HOST"))
      BigtableDataSettings.newBuilder().setProjectId("fake").setInstanceId("fake").build()
    else {
      val builder = BigtableDataSettings
        .newBuilder()
        .setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.getApplicationDefault))
        .setProjectId(ServiceOptions.getDefaultProjectId)
        .setInstanceId(sys.env("BIGTABLE_INSTANCE"))
        .setAppProfileId(sys.env.getOrElse("BIGTABLE_APP_PROFILE", "default"))

      val stubSettings = builder.stubSettings()
      val readRowsRetrySettings = stubSettings
        .readRowsSettings()
        .getRetrySettings
        .toBuilder
        .setMaxRpcTimeout(org.threeten.bp.Duration.ofSeconds(4L))
        .build()
      val readRowRetrySettings = stubSettings
        .readRowsSettings()
        .getRetrySettings
        .toBuilder
        .setMaxRpcTimeout(org.threeten.bp.Duration.ofSeconds(3L))
        .build()
      val mutateRowRetrySettings = stubSettings
        .readRowsSettings()
        .getRetrySettings
        .toBuilder
        .setMaxRpcTimeout(org.threeten.bp.Duration.ofSeconds(5L))
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
      .bracket(r => runMutate(r) >> runRead(r))(r => IO(r.close())) >>
      IO(ExitCode.Success)

  private def runMutate(dataClient: BigtableDataClient): IO[Unit] = {
    val wrapped = new BigtableDataClientWrapper[IO](dataClient)

    val millis                = System.currentTimeMillis()
    val micros                = millis * 1000L
    val reversedCurrentMillis = Long.MaxValue - millis
    cpuNums.traverse[IO, Unit] { num =>
      val usage = ThreadLocalRandom.current().nextInt(0, 100)
      val tags  = Seq("app:fake,location=asia")

      val rowMutation = RowMutation
        .create(tableId, Seq(userName, num.toString, reversedCurrentMillis.toString).mkString(keySep))
        .setCell(family.metric, qualifiers.percentage, micros, ByteString.copyFrom(Bytes.toBytes(usage)))
        .setCell(family.metric, qualifiers.tags, micros, ByteString.copyFrom(Bytes.toBytes(tags.mkString(","))))

      wrapped.mutateRowAsync(rowMutation)
    } >> IO.unit
  }

  private def runRead(dataClient: BigtableDataClient): IO[Unit] =
    readRows(dataClient).map(_.map(_.toString)).map(_.foreach(println(_)))

  implicit val decodeTags: PrimitiveDecoder[List[String]] = bs =>
    try if (bs == null) Right(Nil) else Right(Bytes.toString(bs.toByteArray).split(",").toList.map(_.trim))
    catch {
      case NonFatal(e) => Left(e)
    }

  private def readRows(dataClient: BigtableDataClient): IO[List[(String, CPU)]] = {
    val wrapped = new BigtableDataClientWrapper[IO](dataClient)

    val now    = Instant.now
    val start  = now.minus(Duration.ofMinutes(3)).toEpochMilli * 1000L
    val end    = now.toEpochMilli * 1000L
    val filter = Filters.FILTERS.timestamp().range().of(start, end)
    val query  = Query.create(tableId).prefix(userName + keySep).filter(filter)

    IO.async(wrapped.readRowsAsync[(String, CPU)](_, query))
  }
}

final case class CPU(metric: Metric)

final case class Metric(percentage: Int, tags: List[String])
