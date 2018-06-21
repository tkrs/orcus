import Dependencies._

lazy val root = (project in file("."))
  .settings(allSettings)
  .settings(noPublishSettings)
  .aggregate(core, monix, `twitter-util`, `cats-effect`, free, iota, example, benchmark)
  .dependsOn(core, monix, `twitter-util`, `cats-effect`, free, iota, example, benchmark)

lazy val allSettings =
  buildSettings ++ baseSettings ++ publishSettings

lazy val buildSettings = Seq(
  name := "orcus",
  organization := "com.github.tkrs",
  scalaVersion := Ver.`scala2.12`,
  crossScalaVersions := Seq(
    Ver.`scala2.11`,
    Ver.`scala2.12`
  ),
  addCompilerPlugin(Pkg.kindProjector)
)

lazy val baseSettings = Seq(
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  fork in Test := true,
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, p)) if p >= 12 =>
        compilerOptions ++ Seq(
          "-Ywarn-extra-implicit",
          "-Ywarn-unused:implicits",
          "-Ywarn-unused:imports",
          "-Ywarn-unused:locals",
          "-Ywarn-unused:params",
          "-Ywarn-unused:patvars",
          "-Ywarn-unused:privates"
        )
      case Some((2, p)) if p >= 11 =>
        compilerOptions
      case _                       =>
        Nil
    }
  },
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
)

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/tkrs/orcus")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
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
  publish := ((): Unit),
  publishLocal := ((): Unit),
  publishArtifact := false
)

lazy val core = project
  .in(file("modules/core"))
  .settings(allSettings)
  .settings(
    description := "orcus core",
    moduleName := "orcus-core",
    name := "core",
  )
  .settings(
    libraryDependencies ++= Seq.concat(
      Seq(
        Pkg.catsCore,
        Pkg.shapeless,
        Pkg.java8Compat,
        Pkg.exportHook,
        Pkg.scalaReflect(scalaVersion.value) % "provided",
        Pkg.hbase % "provided"
      ),
      Pkg.forTest,
    ).map(_.withSources),
  )
  .settings(
    addCompilerPlugin(Pkg.macroParadise cross CrossVersion.patch)
  )

lazy val monix = project
  .in(file("modules/monix"))
  .settings(allSettings)
  .settings(
    description := "orcus monix",
    moduleName := "orcus-monix",
    name := "monix",
  )
  .settings(
    libraryDependencies ++= Seq.concat(
      Seq(
        Pkg.monixEval,
      ),
      Pkg.forTest,
    ).map(_.withSources),
  )
  .dependsOn(core)

lazy val `twitter-util` = project
  .in(file("modules/twitter-util"))
  .settings(allSettings)
  .settings(
    description := "orcus twitter-util",
    moduleName := "orcus-twitter-util",
    name := "twitter-util",
  )
  .settings(
    libraryDependencies ++= Seq.concat(
      Seq(
        Pkg.twitterUtil,
      ),
      Pkg.forTest,
    ).map(_.withSources),
  )
  .dependsOn(core)

lazy val `cats-effect` = project
  .in(file("modules/cats-effect"))
  .settings(allSettings)
  .settings(
    description := "orcus cats-effect",
    moduleName := "orcus-cats-effect",
    name := "cats-effect",
  )
  .settings(
    libraryDependencies ++= Seq.concat(
      Seq(
        Pkg.catsEffect,
      ),
      Pkg.forTest,
    ).map(_.withSources),
  )
  .dependsOn(core)

lazy val free = project
  .in(file("modules/free"))
  .settings(allSettings)
  .settings(
    description := "orcus free",
    moduleName := "orcus-free",
    name := "free",
  )
  .settings(
    libraryDependencies ++= Seq.concat(
      Seq(
        Pkg.catsFree,
        Pkg.hbase % "provided"
      ),
      Pkg.forTest,
    ).map(_.withSources),
  )
  .dependsOn(core)

lazy val iota = project
  .in(file("modules/iota"))
  .settings(allSettings)
  .settings(
    description := "orcus iota",
    moduleName := "orcus-iota",
    name := "iota",
  )
  .settings(
    libraryDependencies ++= Seq.concat(
      Seq(
        Pkg.iota,
        Pkg.hbase % "provided",
      ),
      Pkg.forTest,
    ).map(_.withSources),
  )
  .dependsOn(free)

lazy val example = project
  .in(file("modules/example"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    description := "orcus example",
    moduleName := "orcus-example",
    name := "example",
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
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    description := "orcus benchmark",
    moduleName := "orcus-benchmark",
    name := "benchmark"
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
    `cats-effect`,
    monix,
  ).map(_ % "test->test"): _*)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-language:_",
  "-unchecked",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)
