import sbt._

object Dependencies {
  val V = new {
    val scala2 = "2.13.16"
    val scala3 = "3.7.2"

    val kindProjector = "0.13.2"

    val cats          = "2.13.0"
    val catsEffect    = "3.6.1"
    val shapeless     = "2.3.13"
    val shapeless3    = "3.5.0"
    val scalatest     = "3.2.19"
    val scalatestplus = new {
      val scalacheck = "3.2.17.0"
      val mockito    = "3.2.15.0"
    }

    val bigtable         = "2.60.0"
    val logback          = "1.5.18"
    val scalaLogging     = "3.9.5"
    val scalaJava8Compat = "1.0.2"
  }

  lazy val CatsCore       = "org.typelevel"              %% "cats-core"             % V.cats
  lazy val CatsEffect     = "org.typelevel"              %% "cats-effect"           % V.catsEffect
  lazy val Shapeless      = "com.chuusai"                %% "shapeless"             % V.shapeless
  lazy val Shapeless3     = "org.typelevel"              %% "shapeless3-deriving"   % V.shapeless3
  lazy val Java8Compat    = "org.scala-lang.modules"     %% "scala-java8-compat"    % V.scalaJava8Compat
  lazy val Bigtable       = "com.google.cloud"            % "google-cloud-bigtable" % V.bigtable
  lazy val LogbackClassic = "ch.qos.logback"              % "logback-classic"       % V.logback
  lazy val Logging        = "com.typesafe.scala-logging" %% "scala-logging"         % V.scalaLogging

  lazy val Scalatest  = "org.scalatest"     %% "scalatest"       % V.scalatest
  lazy val Scalacheck = "org.scalatestplus" %% "scalacheck-1-17" % V.scalatestplus.scalacheck
  lazy val Mockito    = "org.scalatestplus" %% "mockito-4-6"     % V.scalatestplus.mockito

  lazy val TestDeps = Seq(Scalatest, Scalacheck, Mockito).map(_ % Test)
}
