import sbt._

object Dependencies {
  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
  lazy val awsS3 = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.289"
  lazy val typesafeConfig =  "com.typesafe" % "config" % "1.3.3"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
  lazy val equalsVerifier = "nl.jqno.equalsverifier" % "equalsverifier" % "2.4.3" % Test
  lazy val scalaJava8Compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0" % Test
  lazy val scalaCheck =  "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
  lazy val scalaMock = "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test
}

