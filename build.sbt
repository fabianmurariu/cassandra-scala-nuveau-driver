organization := "underscore.io"

name := "cassandra-scala-nuveau-driver"

version := "1.0.0"

val commonSettings = Seq(
  scalaVersion := "2.11.5",
  resolvers += "Typesafe release" at "http://repo.typesafe.com/typesafe/releases/",
  scalacOptions ++= Seq("-deprecation", "-feature", "-language:experimental.macros"),
  fork in Test := false,
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.1.0",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.4",
    "joda-time" % "joda-time" % "2.7",
    "com.typesafe.play" %% "play-iteratees" %"2.4.0-M2",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1" exclude("org.scala-lang", "scala-library"),
    "org.specs2" %% "specs2" % "2.3.12" % "test" exclude("org.scala-lang", "scala-library")
  )
)

lazy val printtreeLib    = project.in(file("printtree/lib")).settings(commonSettings : _*)

lazy val printtree       = project.in(file("printtree/app")).settings(commonSettings : _*).dependsOn(printtreeLib)

lazy val cqlLib          = project.in(file("cql/lib")).settings(commonSettings : _*)

lazy val cql             = project.in(file("cql/app")).settings(commonSettings : _*).dependsOn(cqlLib, printtree)

