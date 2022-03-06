import sbt._

object Dependencies {

  val akkaVersion = "2.6.18"
  val circeVersion = "0.14.1"
  val doobieVersion = "1.0.0-RC2"
  val fs2Version = "3.2.5"
  val h2Version = "2.1.210"
  val jacksonVersion = "2.13.1"
  val log4sVersion = "1.8.2"
  val logbackVersion = "1.2.11"
  val munitVersion = "0.7.29"
  val scalaTestVersion = "3.2.11"

  val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  )

  val munit = Seq(
    "org.scalameta" %% "munit" % munitVersion,
    "org.scalameta" %% "munit-scalacheck" % munitVersion
  )

  val fs2 = Seq(
    "co.fs2" %% "fs2-core" % fs2Version
  )

  val fs2io = Seq(
    "co.fs2" %% "fs2-io" % fs2Version
  )

  // https://github.com/Log4s/log4s;ASL 2.0
  val loggingApi = Seq(
    "org.log4s" %% "log4s" % log4sVersion
  )

  val logback = Seq(
    "ch.qos.logback" % "logback-classic" % logbackVersion
  )

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % doobieVersion
  )
  val h2 = Seq(
    "com.h2database" % "h2" % h2Version
  )

  val circe = Seq(
    "io.circe" %% "circe-core" % circeVersion
  )
  val circeAll = Seq(
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion
  )

  val jacksonAll = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion % Test
  )

  val akkaAll = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
  )
}
