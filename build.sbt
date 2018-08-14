import sbt.Keys.javacOptions
import Dependencies._

lazy val commonSettings = Seq(
  organization := "com.nike.moirai",
  organizationName := "Nike",
  organizationHomepage := Some(url("http://engineering.nike.com")),
  scalaVersion := "2.12.3",
  javacOptions ++= Seq("-source", "1.8"),
  crossPaths := false,
  autoScalaLibrary := false,
  bintrayOrganization := Some("nike"),
  bintrayReleaseOnPublish := false,
  publishMavenStyle := true,
  licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"),
  homepage := Some(url("https://github.com/Nike-Inc/moirai")),
  startYear := Some(2017),
  description := "A feature-flag and resource-reloading library for the JVM",
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/Nike-Inc/moirai"),
      "scm:git@github.com:Nike-Inc/moirai.git"
    )
  ),
  developers := List(
    Developer(
      id = "jrduncans",
      name = "Stephen Duncan Jr",
      email = "jrduncans@stephenduncanjr.com",
      url = url("https://github.com/jrduncans")
    )
  )
)

lazy val moirai = (project in file("."))
  .settings(commonSettings)
  .settings(
    publishArtifact := false,
    // Replace tasks to work around https://github.com/sbt/sbt-bintray/issues/93
    bintrayRelease := (),
    bintrayEnsureBintrayPackageExists := (),
    bintrayEnsureLicenses := (),
    jacocoAggregateReportSettings := JacocoReportSettings(
      title = "Moirai Project Coverage",
      formats = Seq(JacocoReportFormats.ScalaHTML, JacocoReportFormats.XML)
    ).withThresholds(
      JacocoThresholds(
        instruction = 65,
        method = 80,
        branch = 50,
        complexity = 65,
        line = 80,
        clazz = 90)
    )
  )
  .aggregate(`moirai-core`, `moirai-s3`, `moirai-typesafeconfig`, `moirai-riposte-example`)

lazy val `moirai-core` = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      slf4jApi,
      scalaTest,
      equalsVerifier,
      scalaJava8Compat,
      scalaCheck,
      logback
    )
  )

lazy val `moirai-s3` = project
  .dependsOn(`moirai-core`)
  .settings(commonSettings)
  .settings(
    description := "Support for loading Moirai resources from S3",
    libraryDependencies ++= Seq(
      awsS3,
      scalaTest,
      scalaMock
    )
  )

lazy val `moirai-typesafeconfig` = project
  .dependsOn(`moirai-core`)
  .settings(commonSettings)
  .settings(
    description := "Support for reading Moirai configuration using Typesafe Config",
    libraryDependencies ++= Seq(
      typesafeConfig,
      scalaTest
    )
  )

lazy val `moirai-riposte-example` = project
  .dependsOn(`moirai-core`, `moirai-typesafeconfig`)
  .settings(commonSettings)
  .settings(
    description := "Moirai usage example using Riposte",
    libraryDependencies ++= Seq(
      riposte,
      riposteTypesafeConfig,
      riposteCore,
      riposteAsyncClient,
      googleFindbugsJsr305Version,
      elApi,
      elImpl,
      junit,
      assertJ,
      mockito,
      junitInterface,
      backstopper,
      logback),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "+q", "+n", "+v")

  )