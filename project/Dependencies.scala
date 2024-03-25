import sbt._

object Dependencies {
  val V = new {
    val `scala2.13` = "2.13.13"
    val `scala2.12` = "2.12.19"
    val scala3      = "3.3.3"

    val kindProjector   = "0.13.2"
    val organizeImports = "0.6.0"

    val cats       = "2.10.0"
    val catsEffect = "3.5.4"
    val shapeless  = "2.3.10"
    val shapeless3 = "3.4.1"
    val scalatest  = "3.2.18"
    val scalatestplus = new {
      val scalacheck = "3.2.17.0"
      val mockito    = "3.2.15.0"
    }

    val bigtable         = "2.31.0"
    val logback          = "1.5.3"
    val scalaLogging     = "3.9.5"
    val scalaJava8Compat = "1.0.2"
  }

  lazy val OrganizeImports = "com.github.liancheng" %% "organize-imports" % V.organizeImports

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
