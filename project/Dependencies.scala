import sbt._

object Dependencies {
  val Ver = new {
    val `scala2.12` = "2.12.4"
    val `scala2.11` = "2.11.11"
    val scalafmt    = "1.2.0"
    val cats        = "0.9.0"
    val shapeless   = "2.3.2"
    val scalacheck  = "1.13.5"
    val scalatest   = "3.0.4"
  }

  val Pkg = new {
    lazy val catsCore   = "org.typelevel"  %% "cats-core"  % Ver.cats
    lazy val shapeless  = "com.chuusai"    %% "shapeless"  % Ver.shapeless
    lazy val scalatest  = "org.scalatest"  %% "scalatest"  % Ver.scalatest
    lazy val scalacheck = "org.scalacheck" %% "scalacheck" % Ver.scalacheck
  }
}
