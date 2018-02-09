import sbt._

object Dependencies {
  val Ver = new {
    val `scala2.12`      = "2.12.4"
    val `scala2.11`      = "2.11.12"
    val scalafmt         = "1.2.0"
    val cats             = "1.0.0"
    val iota             = "0.3.4"
    val shapeless        = "2.3.3"
    val scalacheck       = "1.13.5"
    val scalatest        = "3.0.4"
    val mockito          = "2.13.0"
    val kindProjector    = "0.9.6"
    val hbase            = "2.0.0-beta-1"
    val bigtable         = "1.1.0"
    val logback          = "1.2.3"
    val scalaJava8Compat = "0.8.0"
  }

  val Pkg = new {
    lazy val catsCore       = "org.typelevel"             %% "cats-core"          % Ver.cats
    lazy val catsFree       = "org.typelevel"             %% "cats-free"          % Ver.cats
    lazy val iota           = "io.frees"                  %% "iota-core"          % Ver.iota
    lazy val shapeless      = "com.chuusai"               %% "shapeless"          % Ver.shapeless
    lazy val java8Compat    = "org.scala-lang.modules"    %% "scala-java8-compat" % Ver.scalaJava8Compat
    lazy val scalatest      = "org.scalatest"             %% "scalatest"          % Ver.scalatest
    lazy val scalacheck     = "org.scalacheck"            %% "scalacheck"         % Ver.scalacheck
    lazy val mockito        = "org.mockito"               % "mockito-core"        % Ver.mockito
    lazy val kindProjector  = "org.spire-math"            %% "kind-projector"     % Ver.kindProjector
    lazy val hbase          = "org.apache.hbase"          % "hbase-client"        % Ver.hbase
    lazy val bigtable       = "com.google.cloud.bigtable" % "bigtable-hbase-1.x"  % Ver.bigtable
    lazy val logbackClassic = "ch.qos.logback"            % "logback-classic"     % Ver.logback

    lazy val forTest = Seq(scalatest, scalacheck, mockito).map(_ % "test")
  }
}
