import Dependencies._

ThisBuild / organization := "com.github.tkrs"
ThisBuild / scalaVersion := Ver.`scala2.12`
ThisBuild / crossScalaVersions := Seq(
  Ver.`scala2.11`,
  Ver.`scala2.12`
)
ThisBuild / libraryDependencies ++= Pkg.forTest ++ Seq(
  compilerPlugin(Pkg.kindProjector),
  compilerPlugin(Pkg.macroParadise cross CrossVersion.patch)
)
ThisBuild / resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
ThisBuild / scalacOptions ++= compilerOptions ++ {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, p)) if p >= 12 => warnCompilerOptions
    case _                       => Nil
  }
}
ThisBuild / Test / fork := true

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-language:_",
  "-unchecked",
  "-Xcheckinit",
  "-Xfuture",
  "-Ypartial-unification",
)

lazy val warnCompilerOptions = Seq(
  "-Xlint",
  "-Xfatal-warnings",
  "-Yno-adapted-args",
  "-Ywarn-unused:_",
  "-Ywarn-extra-implicit",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
)

lazy val orcus = project.in(file("."))
  .settings(noPublishSettings)
  .settings(
    Compile / console / scalacOptions --= warnCompilerOptions,
    Compile / console / scalacOptions += "-Yrepl-class-based"
  )
  .aggregate(core, `arrows-twitter`, `twitter-util`, monix, `cats-effect`, free, iota, example, benchmark)
  .dependsOn(core, `arrows-twitter`, `twitter-util`, monix, `cats-effect`, free, iota, example, benchmark)

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/tkrs/orcus")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
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
  pgpSecretRing := sys.env.get("PGP_SECRET_RING").fold(pgpSecretRing.value)(file)
)

lazy val noPublishSettings = Seq(
  publish / skip := true
)

lazy val core = project
  .in(file("modules/core"))
  .settings(publishSettings)
  .settings(
    description := "orcus core",
    moduleName := "orcus-core",
  )
  .settings(
    libraryDependencies ++= Seq.concat(
      Seq(
        Pkg.catsCore,
        Pkg.shapeless,
        Pkg.java8Compat,
        Pkg.exportHook,
        Pkg.scalaReflect(scalaVersion.value),
        Pkg.hbase % "provided"
      ),
    ).map(_.withSources),
  )

lazy val monix = project
  .in(file("modules/monix"))
  .settings(publishSettings)
  .settings(
    description := "orcus monix",
    moduleName := "orcus-monix",
  )
  .settings(
    libraryDependencies ++= Seq(
      Pkg.monixEval,
    ).map(_.withSources),
  )
  .dependsOn(core)

lazy val `twitter-util` = project
  .in(file("modules/twitter-util"))
  .settings(publishSettings)
  .settings(
    description := "orcus twitter-util",
    moduleName := "orcus-twitter-util",
  )
  .settings(
    libraryDependencies ++= Seq(
      Pkg.twitterUtil,
    ).map(_.withSources),
  )
  .dependsOn(core)

lazy val `arrows-twitter` = project
  .in(file("modules/arrows-twitter"))
  .settings(publishSettings)
  .settings(
    description := "orcus arrows-twitter",
    moduleName := "orcus-arrows-twitter",
  )
  .settings(
    libraryDependencies ++= Seq(
      Pkg.twitterArrows,
    ).map(_.withSources),
  )
  .dependsOn(core)

lazy val `cats-effect` = project
  .in(file("modules/cats-effect"))
  .settings(publishSettings)
  .settings(
    description := "orcus cats-effect",
    moduleName := "orcus-cats-effect",
  )
  .settings(
    libraryDependencies ++= Seq(
      Pkg.catsEffect,
    ).map(_.withSources),
  )
  .dependsOn(core)

lazy val free = project
  .in(file("modules/free"))
  .settings(publishSettings)
  .settings(
    description := "orcus free",
    moduleName := "orcus-free",
  )
  .settings(
    libraryDependencies ++= Seq(
        Pkg.catsFree,
        Pkg.hbase % "provided"
    ).map(_.withSources),
  )
  .dependsOn(core)

lazy val iota = project
  .in(file("modules/iota"))
  .settings(publishSettings)
  .settings(
    description := "orcus iota",
    moduleName := "orcus-iota",
  )
  .settings(
    libraryDependencies ++= Seq(
      Pkg.iota,
      Pkg.hbase % "provided",
    ).map(_.withSources),
  )
  .dependsOn(free)

lazy val example = project
  .in(file("modules/example"))
  .settings(noPublishSettings)
  .settings(
    description := "orcus example",
    moduleName := "orcus-example",
  )
  .settings(
    libraryDependencies ++= Seq(
      Pkg.bigtable,
      Pkg.logbackClassic
    ).map(_.withSources)
  )
  .settings(
    fork := true,
    coverageEnabled := false
  )
  .dependsOn(iota, `cats-effect`)

lazy val benchmark = (project in file("modules/benchmark"))
  .settings(noPublishSettings)
  .settings(
    description := "orcus benchmark",
    moduleName := "orcus-benchmark",
  )
  .settings(
    libraryDependencies ++= Seq(
      Pkg.java8Compat,
      Pkg.hbase,
      Pkg.catbirdUtil,
    )
  )
  .enablePlugins(JmhPlugin)
  .dependsOn(Seq(
    iota,
    `twitter-util`,
    `arrows-twitter`,
    `cats-effect`,
    monix,
  ).map(_ % "test->test"): _*)
