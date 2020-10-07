import sbt._

object Dependencies {
  val V = new {
    val `scala2.13` = "2.13.3"
    val `scala2.12` = "2.12.12"

    val kindProjector   = "0.11.0"
    val organizeImports = "0.4.0"

    val cats        = "2.1.1"
    val monix       = "3.2.2"
    val twitterUtil = "20.6.0"
    val catsEffect  = "2.1.4"
    val shapeless   = "2.3.3"
    val scalatest   = "3.2.2"
    val scalatestplus = new {
      val scalacheck = "3.2.2.0"
      val mockito    = "3.2.2.0"
    }

    val hbase            = "2.2.3"
    val bigtableHBase    = "1.15.0"
    val bigtable         = "1.15.0"
    val logback          = "1.2.3"
    val scalaLogging     = "3.9.2"
    val scalaJava8Compat = "0.9.0"
  }

  lazy val KindProjector   = ("org.typelevel"       %% "kind-projector"   % V.kindProjector).cross(CrossVersion.full)
  lazy val OrganizeImports = "com.github.liancheng" %% "organize-imports" % V.organizeImports

  lazy val CatsCore       = "org.typelevel"              %% "cats-core"             % V.cats
  lazy val CatsFree       = "org.typelevel"              %% "cats-free"             % V.cats
  lazy val CatsEffect     = "org.typelevel"              %% "cats-effect"           % V.catsEffect
  lazy val MonixEval      = "io.monix"                   %% "monix-eval"            % V.monix
  lazy val TwitterUtil    = "com.twitter"                %% "util-core"             % V.twitterUtil
  lazy val CatbirdUtil    = "io.catbird"                 %% "catbird-util"          % V.twitterUtil
  lazy val Shapeless      = "com.chuusai"                %% "shapeless"             % V.shapeless
  lazy val Java8Compat    = "org.scala-lang.modules"     %% "scala-java8-compat"    % V.scalaJava8Compat
  lazy val Hbase          = "org.apache.hbase"            % "hbase-client"          % V.hbase
  lazy val BigtableHBase  = "com.google.cloud.bigtable"   % "bigtable-hbase-2.x"    % V.bigtableHBase
  lazy val Bigtable       = "com.google.cloud"            % "google-cloud-bigtable" % V.bigtable
  lazy val LogbackClassic = "ch.qos.logback"              % "logback-classic"       % V.logback
  lazy val Logging        = "com.typesafe.scala-logging" %% "scala-logging"         % V.scalaLogging

  lazy val Scalatest  = "org.scalatest"     %% "scalatest"       % V.scalatest
  lazy val Scalacheck = "org.scalatestplus" %% "scalacheck-1-14" % V.scalatestplus.scalacheck
  lazy val Mockito    = "org.scalatestplus" %% "mockito-3-4"     % V.scalatestplus.mockito

  lazy val TestDeps = Seq(Scalatest, Scalacheck, Mockito).map(_ % Test)
}
