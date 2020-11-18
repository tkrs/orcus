# orcus

[![Build Status](https://travis-ci.com/tkrs/orcus.svg?branch=master)](https://travis-ci.com/tkrs/orcus)
[![codecov](https://codecov.io/gh/tkrs/orcus/branch/master/graph/badge.svg)](https://codecov.io/gh/tkrs/orcus)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tkrs/orcus-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.tkrs/orcus-core_2.13)
[![Latest version](https://index.scala-lang.org/tkrs/orcus/latest.svg)](https://index.scala-lang.org/tkrs/orcus/orcus-core)

orcus is a library to interact with HBase/Bigtable built atop [HBase Java Client](https://github.com/apache/hbase/tree/rel/2.0.0-beta-1)/[Bigtable Java Client](https://github.com/googleapis/java-bigtable) for connecting to an HBase/Bigtable instance. Also, it has a feature to automatically derive a result object to an arbitrary type object.

## How to use it

### HBase

```scala
libraryDependencies += Seq(
  "com.github.tkrs" %% "orcus-hbase" % "x.y.z"
)
```

And, so look at this [example](https://github.com/tkrs/orcus/blob/master/modules/hbase-example/src/main/scala/example/FreeMain.scala)

### Bigtable

```scala
libraryDependencies += Seq(
  "com.github.tkrs" %% "orcus-bigtable" % "x.y.z"
)
```

And, so look at this [example](https://github.com/tkrs/orcus/blob/master/modules/bigtable-example/src/main/scala/orcus/example/Main.scala)

LICENSE

MIT
