import Dependencies._

lazy val orcus = project
  .in(file("."))
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(
    ThisBuild / organization := "com.github.tkrs",
    ThisBuild / scalaVersion := V.`scala2.13`,
    ThisBuild / crossScalaVersions := Seq(V.`scala2.13`, V.`scala2.12`),
    ThisBuild / resolvers ++= Seq(Resolver.sonatypeRepo("releases"), Resolver.sonatypeRepo("snapshots")),
    ThisBuild / libraryDependencies ++= TestDeps ++ Seq(Hbase % Provided, compilerPlugin(KindProjector)),
    ThisBuild / scalacOptions ++= compilerOptions ++ warnCompilerOptions ++ {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => Nil
        case _                       => obsoletedOptions
      }
    },
    Compile / console / scalacOptions --= warnCompilerOptions,
    Compile / console / scalacOptions += "-Yrepl-class-based",
    ThisBuild / fork := true,
    ThisBuild / scalafmtOnCompile := true,
    ThisBuild / scalafixOnCompile := true,
    ThisBuild / scalafixDependencies += OrganizeImports,
    ThisBuild / semanticdbEnabled := true,
    ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
  )
  .aggregate(core,
             hbase,
             bigtable,
             `cats-effect`,
             `hbase-cats-free`,
             monix,
             `twitter-util`,
             `hbase-example`,
             `bigtable-example`,
             benchmark
  )
  .dependsOn(core,
             hbase,
             bigtable,
             `cats-effect`,
             `hbase-cats-free`,
             monix,
             `twitter-util`,
             `hbase-example`,
             `bigtable-example`,
             benchmark
  )

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
          CatsCore,
          Shapeless,
          Java8Compat
        )
      )
      .map(_.withSources)
  )

lazy val hbase = project
  .in(file("modules/hbase"))
  .settings(publishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus hbase",
    moduleName := "orcus-hbase",
    libraryDependencies ++= Seq(
      Hbase.withSources() % Provided
    )
  )
  .dependsOn(core)

lazy val monix = project
  .in(file("modules/monix"))
  .settings(publishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus monix",
    moduleName := "orcus-monix"
  )
  .settings(
    libraryDependencies += MonixEval.withSources
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
    libraryDependencies += TwitterUtil.withSources
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
    libraryDependencies += CatsEffect.withSources
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val `hbase-cats-free` = project
  .in(file("modules/hbase-cats-free"))
  .settings(publishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus cats-free",
    moduleName := "orcus-cats-free"
  )
  .settings(
    libraryDependencies += CatsFree.withSources
  )
  .dependsOn(hbase)

lazy val bigtable = project
  .in(file("modules/bigtable"))
  .settings(publishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus bigtable",
    moduleName := "orcus-bigtable"
  )
  .settings(
    libraryDependencies += Bigtable.withSources
  )
  .dependsOn(core)

lazy val `hbase-example` = project
  .in(file("modules/hbase-example"))
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus example",
    moduleName := "orcus-example"
  )
  .settings(
    libraryDependencies ++= Seq(
      Logging,
      LogbackClassic
    ).map(_.withSources)
  )
  .settings(
    fork := true,
    coverageEnabled := false
  )
  .settings(
    scalacOptions -= "-Xfatal-warnings"
  )
  .dependsOn(`cats-effect`, `hbase-cats-free`)

lazy val `bigtable-example` = project
  .in(file("modules/bigtable-example"))
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(crossVersionSharedSources)
  .settings(
    description := "orcus bigtable-example",
    moduleName := "orcus-bigtable-example"
  )
  .settings(
    libraryDependencies ++= Seq(
      Logging,
      LogbackClassic
    ).map(_.withSources)
  )
  .settings(
    fork := true,
    coverageEnabled := false
  )
  .settings(
    scalacOptions -= "-Xfatal-warnings"
  )
  .dependsOn(bigtable, `cats-effect`)

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
      Java8Compat,
      Hbase,
      CatbirdUtil
    )
  )
  .enablePlugins(JmhPlugin)
  .dependsOn(
    Seq(
      hbase,
      `twitter-util`,
      `cats-effect`,
      monix
    ).map(_ % "test->test"): _*
  )

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
  "-Ywarn-unused:_",
  "-Ywarn-extra-implicit",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen"
)

lazy val obsoletedOptions = Seq("-Xfuture", "-Ypartial-unification", "-Yno-adapted-args", "-Ywarn-inaccessible")

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/tkrs/orcus")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := (_ => false),
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
    </developers>
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
            case Some((2, n)) if n >= 13 => Seq(file(dir.getPath + "_2.13+"))
            case _                       => Seq(file(dir.getPath + "_2.12-"))
          }
      }
    }
  }
