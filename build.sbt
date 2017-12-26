import Dependencies._

lazy val root = (project in file("."))
  .settings(allSettings)
  .settings(noPublishSettings)
  .aggregate(core, free, example, benchmark)
  .dependsOn(core, free, example, benchmark)

lazy val allSettings =
  buildSettings ++ baseSettings ++ publishSettings

lazy val buildSettings = Seq(
  name := "orcus",
  organization := "com.github.tkrs",
  scalaVersion := Ver.`scala2.12`,
  crossScalaVersions := Seq(
    Ver.`scala2.11`,
    Ver.`scala2.12`,
  ),
  addCompilerPlugin(Pkg.kindProjector),
)

lazy val baseSettings = Seq(
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  fork in Test := true,
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, p)) if p >= 12 => compilerOptions ++ Seq(
        "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
        "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
        "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
        "-Ywarn-unused:locals",              // Warn if a local definition is unused.
        "-Ywarn-unused:params",              // Warn if a value parameter is unused.
        "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
        "-Ywarn-unused:privates",            // Warn if a private member is unused.
      )
      case Some((2, p)) if p >= 11 => compilerOptions
      case _ => Nil
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
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/tkrs/orcus"),
      "scm:git:git@github.com:tkrs/orcus.git",
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
) ++ credentialSettings

lazy val credentialSettings = Seq(
  credentials ++= (for {
    username <- sys.env.get("SONATYPE_USERNAME")
    password <- sys.env.get("SONATYPE_PASSWORD")
  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
)

lazy val noPublishSettings = Seq(
  publish := ((): Unit),
  publishLocal := ((): Unit),
  publishArtifact := false,
)

lazy val core = project.in(file("modules/core"))
  .settings(allSettings)
  .settings(
    description := "orcus core",
    moduleName := "orcus-core",
    name := "core",
    libraryDependencies ++= Seq(
      Pkg.catsCore,
      Pkg.shapeless,
      Pkg.hbase % "provided",
    ).map(_.withSources),
    libraryDependencies ++= Pkg.forTest
  )

lazy val free = project.in(file("modules/free"))
  .settings(allSettings)
  .settings(
    description := "orcus free",
    moduleName := "orcus-free",
    name := "free",
    libraryDependencies ++= Seq(
      Pkg.catsFree,
      Pkg.hbase % "provided",
    ).map(_.withSources),
    libraryDependencies ++= Pkg.forTest
  )
  .dependsOn(core)

lazy val example = project.in(file("modules/example"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    description := "orcus example",
    moduleName := "orcus-example",
    name := "example",
    fork := true,
    libraryDependencies ++= Seq(
      Pkg.bigtable,
      Pkg.logbackClassic,
    ).map(_.withSources)
  )
  .settings(
    coverageEnabled := false
  )
  .dependsOn(free)

lazy val benchmark = (project in file("modules/benchmark"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    description := "orcus benchmark",
    moduleName := "orcus-benchmark",
    name := "benchmark",
  )
  .enablePlugins(JmhPlugin)
  .dependsOn(free)

// See https://tpolecat.github.io/2017/04/25/scalac-flags.html
lazy val compilerOptions = Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xfuture",                          // Turn on future language features.
  "-Xlint",
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification",             // Enable partial unification in type constructor inference
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
)
