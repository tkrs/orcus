# orcus

[![Build Status](https://travis-ci.org/tkrs/orcus.svg?branch=master)](https://travis-ci.org/tkrs/orcus)
[![codecov](https://codecov.io/gh/tkrs/orcus/branch/master/graph/badge.svg)](https://codecov.io/gh/tkrs/orcus)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tkrs/orcus-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.tkrs/orcus-core_2.12)
[![Latest version](https://index.scala-lang.org/tkrs/orcus/latest.svg)](https://index.scala-lang.org/tkrs/orcus/orcus-core)

orcus is a library to interact with HBase/Bigtable built atop [HBase Java Client](https://github.com/apache/hbase/tree/rel/2.0.0-beta-1) for connecting to an HBase/Bigtable instance. Also, it has a feature to automatically derive each result object to an object of an arbitrary type.

## How to use it

### HBase

Firstly, this library depends on "provided" HBase client on your project. Thus, you must to add an HBase client to your `build.sbt` in addition to `orcus-core`. For example:

```
libraryDependencies += Seq(
  "com.github.tkrs" %% "orcus-core" % "x.y.z",
  "org.apache.hbase" % "hbase-client" % "a.b.c"
)
```

And, so look at this [example](https://github.com/tkrs/orcus/blob/master/modules/example/src/main/scala/example/FreeMain.scala)

### Bigtable

If you use the [google-cloud-bigtable](https://github.com/googleapis/java-bigtable), you can use the `orcus-bigtable`

```
libraryDependencies += Seq(
  "com.github.tkrs" %% "orcus-bigtable" % "x.y.z"
)
```

And, so look at this [example](https://github.com/tkrs/orcus/blob/master/modules/bigtable-example/src/main/scala/example/Main.scala)

LICENSE

MIT
