import sbt._

object Dependencies {
  val logbackVersion = "1.2.3"
  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
  lazy val awsS3 = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.289"
  lazy val typesafeConfig =  "com.typesafe" % "config" % "1.3.3"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
  lazy val equalsVerifier = "nl.jqno.equalsverifier" % "equalsverifier" % "2.4.3" % Test
  lazy val scalaJava8Compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0" % Test
  lazy val scalaCheck =  "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
  lazy val logback = "ch.qos.logback" % "logback-classic" % logbackVersion % Test
  lazy val scalaMock = "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test

  val riposteVersion = "0.12.0"
  lazy val riposte = "com.nike.riposte" % "riposte-spi" % riposteVersion
  lazy val riposteTypesafeConfig = "com.nike.riposte" % "riposte-typesafe-config" % riposteVersion
  lazy val riposteCore = "com.nike.riposte" % "riposte-core" % riposteVersion
  lazy val riposteAsyncClient = "com.nike.riposte" % "riposte-async-http-client"  % riposteVersion
  lazy val googleFindbugsJsr305Version = "com.google.code.findbugs" % "jsr305" % "3.0.2"

  val backstopperVersion = "0.11.4"
  lazy val backstopper = "com.nike.backstopper" % "backstopper-reusable-tests" % backstopperVersion

  val elApiVersion = "2.2.1-b04" // The el-api and el-impl are needed for the JSR 303 validation
  val elImplVersion = "2.2.1-b05"
  lazy val elApi = "javax.el" % "el-api" % elApiVersion
  lazy val elImpl = "org.glassfish.web" % "el-impl" % elImplVersion

  val junitVersion = "4.11"
  val mockitoVersion = "1.10.8"
  val assertJVersion = "3.0.0"

  lazy val junit = "junit" % "junit-dep" % junitVersion % Test
  lazy val assertJ = "org.assertj" % "assertj-core" % assertJVersion % Test
  lazy val mockito = "org.mockito" % "mockito-core" % mockitoVersion % Test

  val junitInterfaceVersion = "0.11"
  lazy val junitInterface = "com.novocode" % "junit-interface" % junitInterfaceVersion % Test

}

