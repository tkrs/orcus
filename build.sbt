import Dependencies._

ThisBuild / organization := "com.github.tkrs"
ThisBuild / scalaVersion := Ver.`scala2.12`
ThisBuild / crossScalaVersions := Seq(
  Ver.`scala2.11`,
  Ver.`scala2.12`
  // Ver.`scala2.13`,
)
ThisBuild / resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
ThisBuild / libraryDependencies ++= Pkg.forTest(scalaVersion.value) ++ {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => Seq(Pkg.hbase % "provided", compilerPlugin(Pkg.kindProjector))
    case _             => Seq(Pkg.hbase % "provided", compilerPlugin(Pkg.kindProjector), compilerPlugin(Pkg.macroParadise))
  }
}
ThisBuild / scalacOptions ++= compilerOptions ++ {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => Seq("-Ymacro-annotations")
    case Some((2, 12)) =>
      warnCompilerOptions ++ Seq(
        "-Ypartial-unification",
        "-Yno-adapted-args",
        "-Xfuture",
        "-Ywarn-inaccessible",
        "-Ywarn-extra-implicit",
        "-Ywarn-dead-code",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-Ywarn-numeric-widen"
      )

    case _ => Seq("-Ypartial-unification")
  }
}
ThisBuild / Test / fork := true

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:_",
  "-unchecked",
  "-Xcheckinit"
)

lazy val warnCompilerOptions = Seq(
  "-Xlint",
  "-Xfatal-warnings",
  "-Ywarn-unused:_",
  "-Ywarn-value-discard"
)

lazy val orcus = project
  .in(file("."))
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(
    Compile / console / scalacOptions --= warnCompilerOptions,
    Compile / console / scalacOptions += "-Yrepl-class-based"
  )
  .aggregate(core, `arrows-twitter`, `twitter-util`, monix, `cats-effect`, `cats-free`, example, benchmark)
  .dependsOn(core, `arrows-twitter`, `twitter-util`, monix, `cats-effect`, `cats-free`, example, benchmark)

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/tkrs/orcus")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ =>
    false
  },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots".at(nexus + "content/repositories/snapshots"))
    else
      Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/tkrs/orcus"),
      "scm:git:git@github.com:tkrs/orcus.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>tkrs</id>
        <name>Takeru Sato</name>
        <url>https://github.com/tkrs</url>
      </developer>
    </developers>,
  pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray),
  pgpSecretRing := sys.env.get("PGP_SECRET_RING").fold(pgpSecretRing.value)(file),
  Compile / doc / sources := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => Nil
      case _             => (Compile / doc / sources).value
    }
  }
)

lazy val noPublishSettings = Seq(
  publish / skip := true
)

lazy val crossVersionSharedSources: Seq[Setting[_]] =
  Seq(Compile, Test).map { sc =>
    (sc / unmanagedSourceDirectories) ++= {
      (sc / unmanagedSourceDirectories).value.flatMap { dir =>
        if (dir.getName != "scala") Seq(dir)
        else
          CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((2, 13)) => Seq(file(dir.getPath + "_2.13"))
            case Some((2, 12)) => Seq(file(dir.getPath + "_2.12"))
            case _             => Seq(file(dir.getPath + "_2.11"))
          }
      }
    }
  }

lazy val core = project
  .in(file("modules/core"))
  .settings(publishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus core",
    moduleName := "orcus-core"
  )
  .settings(
    libraryDependencies ++= Seq
      .concat(
        Seq(
          Pkg.catsCore,
          Pkg.shapeless,
          Pkg.java8Compat,
          Pkg.exportHook,
          Pkg.scalaReflect(scalaVersion.value)
        )
      )
      .map(_.withSources)
  )

lazy val monix = project
  .in(file("modules/monix"))
  .settings(publishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus monix",
    moduleName := "orcus-monix"
  )
  .settings(
    libraryDependencies += Pkg.monixEval.withSources
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val `twitter-util` = project
  .in(file("modules/twitter-util"))
  .settings(publishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus twitter-util",
    moduleName := "orcus-twitter-util"
  )
  .settings(
    libraryDependencies += Pkg.twitterUtil.withSources
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val `arrows-twitter` = project
  .in(file("modules/arrows-twitter"))
  .settings(publishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus arrows-twitter",
    moduleName := "orcus-arrows-twitter"
  )
  .settings(
    libraryDependencies += Pkg.twitterArrows.withSources
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val `cats-effect` = project
  .in(file("modules/cats-effect"))
  .settings(publishSettings)
  .settings(
    description := "orcus cats-effect",
    moduleName := "orcus-cats-effect"
  )
  .settings(
    libraryDependencies += Pkg.catsEffect.withSources
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val `cats-free` = project
  .in(file("modules/cats-free"))
  .settings(publishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus cats-free",
    moduleName := "orcus-cats-free"
  )
  .settings(
    libraryDependencies += Pkg.catsFree.withSources
  )
  .dependsOn(core)

lazy val example = project
  .in(file("modules/example"))
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus example",
    moduleName := "orcus-example"
  )
  .settings(
    libraryDependencies ++= Seq(
      Pkg.bigtable,
      Pkg.logging,
      Pkg.logbackClassic
    ).map(_.withSources)
  )
  .settings(
    fork := true,
    coverageEnabled := false
  )
  .settings(
    scalacOptions -= "-Xfatal-warnings"
  )
  .dependsOn(`cats-effect`, `cats-free`)

lazy val benchmark = (project in file("modules/benchmark"))
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus benchmark",
    moduleName := "orcus-benchmark"
  )
  .settings(
    libraryDependencies ++= Seq(
      Pkg.java8Compat,
      Pkg.hbase,
      Pkg.catbirdUtil
    )
  )
  .enablePlugins(JmhPlugin)
  .dependsOn(
    Seq(
      `twitter-util`,
      `arrows-twitter`,
      `cats-effect`,
      monix
    ).map(_ % "test->test"): _*
  )
