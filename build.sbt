import java.nio.file.Files
import Dependencies._

lazy val orcus = project
  .in(file("."))
  .settings(publish / skip := true)
  .settings(
    inThisBuild(
      Seq(
        version      := Files.readString(file("version.txt").toPath()).trim(),
        organization := "com.github.tkrs",
        homepage     := Some(url("https://github.com/tkrs/orcus")),
        licenses     := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
        developers := List(
          Developer(
            "tkrs",
            "Takeru Sato",
            "type.in.type@gmail.com",
            url("https://github.com/tkrs")
          )
        ),
        scalaVersion       := V.scala3,
        crossScalaVersions := Seq(V.scala2, V.scala3),
        fork               := true,
        scalafmtOnCompile  := true,
        scalafixOnCompile  := true,
        semanticdbEnabled  := true,
        semanticdbVersion  := scalafixSemanticdb.revision,
        dynverSeparator    := "-"
      )
    )
  )
  .settings(
    Compile / console / scalacOptions --= warnCompilerOptions,
    Compile / console / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq("-explain")
        case _            => Seq("-Yrepl-class-based")
      }
    }
  )
  .aggregate(core, bigtable, `cats-effect`, `bigtable-example`)

lazy val shapeless = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) => Shapeless3
    case _            => Shapeless
  }
}

lazy val core = project
  .in(file("modules/core"))
  .settings(sharedSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus core",
    moduleName  := "orcus-core"
  )
  .settings(
    libraryDependencies ++=
      Seq(
        CatsCore,
        shapeless.value,
        Java8Compat
      ).map(_.withSources)
  )

lazy val `cats-effect` = project
  .in(file("modules/cats-effect"))
  .settings(sharedSettings)
  .settings(
    description := "orcus cats-effect",
    moduleName  := "orcus-cats-effect"
  )
  .settings(
    libraryDependencies += CatsEffect.withSources
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val bigtable = project
  .in(file("modules/bigtable"))
  .settings(sharedSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus bigtable",
    moduleName  := "orcus-bigtable"
  )
  .settings(
    libraryDependencies += Bigtable.withSources
  )
  .dependsOn(core)

lazy val `bigtable-example` = project
  .in(file("modules/bigtable-example"))
  .settings(sharedSettings)
  .settings(publish / skip := true)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus bigtable-example",
    moduleName  := "orcus-bigtable-example"
  )
  .settings(
    libraryDependencies ++= Seq(
      Logging,
      LogbackClassic
    ).map(_.withSources)
  )
  .settings(
    fork            := true,
    coverageEnabled := false
  )
  .settings(
    scalacOptions -= "-Xfatal-warnings"
  )
  .dependsOn(bigtable, `cats-effect`)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:higherKinds",
  "-unchecked"
)

lazy val warnCompilerOptions = Seq(
  // "-Xlint",
  "-Xcheckinit",
  // "-Xfatal-warnings",
  "-Wunused:_",
  "-Ywarn-extra-implicit",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen"
)

lazy val obsoletedOptions = Seq("-Xfuture", "-Ypartial-unification", "-Yno-adapted-args", "-Ywarn-inaccessible")

lazy val sharedSettings = Seq(
  scalacOptions ++= compilerOptions ++ {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _))  => Seq("-Wunused:imports")
      case Some((2, 13)) => compilerOptions ++ warnCompilerOptions ++ Seq("-Xsource:3")
      case _             => compilerOptions ++ warnCompilerOptions ++ obsoletedOptions ++ Seq("-Xsource:3")
    }
  },
  libraryDependencies ++= TestDeps
)

lazy val crossVersionSharedSources: Seq[Setting[_]] =
  Seq(Compile, Test).map { sc =>
    (sc / unmanagedSourceDirectories) ++=
      (sc / unmanagedSourceDirectories).value.flatMap { dir =>
        if (dir.getName != "scala") Seq(dir)
        else
          CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((3, _)) => Seq(file(dir.getPath + "_3"))
            case _            => Seq(file(dir.getPath + "_2"))
          }
      }
  }
