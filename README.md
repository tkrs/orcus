# orcus

[![Build Status](https://travis-ci.org/tkrs/orcus.svg?branch=master)](https://travis-ci.org/tkrs/orcus)
[![codecov](https://codecov.io/gh/tkrs/orcus/branch/master/graph/badge.svg)](https://codecov.io/gh/tkrs/orcus)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tkrs/orcus-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.tkrs/orcus-core_2.12)

orcus is a library to interact with HBase built atop Cats, Shapeless, and [HBase Java Client](https://github.com/apache/hbase/tree/rel/2.0.0-beta-1) for connecting to an HBase instance.

## How to use it

Add to your build.sbt

```
libraryDependencies ++= Seq(
  "org.apache.hbase" % "hbase-client" % "2.0.0-beta-2",
  "com.github.tkrs" %% "orcus-core" % "x.y.z"
)
```

If you are using `Free`, you can also use the following module.

```
libraryDependencies ++= Seq(
  "org.apache.hbase" % "hbase-client" % "2.0.0-beta-2",
  "com.github.tkrs" %% "orcus-free" % "x.y.z"
)
```

And, so look at this [example](https://github.com/tkrs/orcus/tree/master/modules/example/src/main/scala/example/bigtable)

LICENSE

MIT
