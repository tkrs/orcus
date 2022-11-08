# orcus

[![CI](https://github.com/tkrs/orcus/workflows/CI/badge.svg)](https://github.com/tkrs/orcus/actions?query=workflow%3ACI)
[![Release](https://github.com/tkrs/orcus/workflows/Release/badge.svg)](https://github.com/tkrs/orcus/actions?query=workflow%3ARelease)
[![Tagging](https://github.com/tkrs/orcus/actions/workflows/tagging.yml/badge.svg)](https://github.com/tkrs/orcus/actions/workflows/tagging.yml)
[![codecov](https://codecov.io/gh/tkrs/orcus/branch/master/graph/badge.svg)](https://codecov.io/gh/tkrs/orcus)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tkrs/orcus-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.tkrs/orcus-core_2.13)
[![Latest version](https://index.scala-lang.org/tkrs/orcus/latest.svg)](https://index.scala-lang.org/tkrs/orcus/orcus-core)

orcus is a library to interact with Bigtable built atop [Bigtable Java Client](https://github.com/googleapis/java-bigtable) for connecting to an Bigtable instance. Also, it has a feature to automatically derive a result object to an arbitrary type object.

## How to use it

```scala
libraryDependencies += Seq(
  "com.github.tkrs" %% "orcus-bigtable" % "x.y.z",
  "com.github.tkrs" %% "orcus-cats-effect" % "x.y.z"
)
```

And, so look at this [example](https://github.com/tkrs/orcus/blob/master/modules/bigtable-example/src/main/scala/orcus/example/Main.scala)

LICENSE

MIT
