import sbt._

object Dependencies {
  val Ver = new {
    val `scala2.13`      = "2.13.0-M4"
    val `scala2.12`      = "2.12.8"
    val `scala2.11`      = "2.11.12"
    val scalafmt         = "1.5.1"
    val cats             = "1.5.0"
    val monix            = "3.0.0-RC1"
    val twitterArrows    = "0.1.23"
    val twitterUtil      = "18.12.0"
    val catsEffect       = "1.1.0"
    val iota             = "0.3.10"
    val shapeless        = "2.3.3"
    val exportHook       = "1.2.0"
    val scalacheck       = "1.14.0"
    val scalatest        = "3.0.5"
    val scalatestSnap    = "3.0.6-SNAP2"
    val mockito          = "2.23.0"
    val kindProjector    = "0.9.9"
    val hbase            = "2.1.0"
    val bigtable         = "1.7.0"
    val logback          = "1.2.3"
    val scalaJava8Compat = "0.9.0"
    val macroParadise    = "2.1.1"
  }

  val Pkg = new {
    lazy val catsCore       = "org.typelevel"             %% "cats-core"          % Ver.cats
    lazy val catsFree       = "org.typelevel"             %% "cats-free"          % Ver.cats
    lazy val catsEffect     = "org.typelevel"             %% "cats-effect"        % Ver.catsEffect
    lazy val monixEval      = "io.monix"                  %% "monix-eval"         % Ver.monix
    lazy val twitterArrows  = "io.trane"                  %% "arrows-twitter"     % Ver.twitterArrows
    lazy val twitterUtil    = "com.twitter"               %% "util-core"          % Ver.twitterUtil
    lazy val catbirdUtil    = "io.catbird"                %% "catbird-util"       % Ver.twitterUtil
    lazy val iota           = "io.frees"                  %% "iota-core"          % Ver.iota
    lazy val shapeless      = "com.chuusai"               %% "shapeless"          % Ver.shapeless
    lazy val exportHook     = "org.typelevel"             %% "export-hook"        % Ver.exportHook
    lazy val java8Compat    = "org.scala-lang.modules"    %% "scala-java8-compat" % Ver.scalaJava8Compat
    lazy val scalacheck     = "org.scalacheck"            %% "scalacheck"         % Ver.scalacheck
    lazy val mockito        = "org.mockito"               % "mockito-core"        % Ver.mockito
    lazy val kindProjector  = "org.spire-math"            %% "kind-projector"     % Ver.kindProjector
    lazy val macroParadise  = "org.scalamacros"           % "paradise"            % Ver.macroParadise cross CrossVersion.patch
    lazy val hbase          = "org.apache.hbase"          % "hbase-client"        % Ver.hbase
    lazy val bigtable       = "com.google.cloud.bigtable" % "bigtable-hbase-2.x"  % Ver.bigtable
    lazy val logbackClassic = "ch.qos.logback"            % "logback-classic"     % Ver.logback

    def scalatest(v: String) = "org.scalatest" %% "scalatest" % (if (v == Ver.`scala2.13`) Ver.scalatestSnap else Ver.scalatest)
    def scalaReflect(v: String) = "org.scala-lang" % "scala-reflect" % v % "provided"
    def forTest(v: String) = Seq(scalatest(v), scalacheck, mockito).map(_ % "test")
  }
}
