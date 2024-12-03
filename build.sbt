import Dependencies._
import Publishing._

lazy val basicSettings = Seq(
  organization := "com.scalapenos",
  licenses := Seq("The MIT License (MIT)" -> url("http://opensource.org/licenses/MIT")),
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq("2.11.11", "2.12.4", "2.13.1"),
  scalacOptions := Seq(
    "-encoding", "utf8",
    "-target:jvm-1.8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-Xlint",
    "-Xlog-reflective-calls",
    "-Ywarn-unused"
  ),
  scalacOptions += {
    if(scalaBinaryVersion.value == "2.13") {
      "-Wunused:imports"
    } else {
      "-Ywarn-unused-import"
    }
  }
)

lazy val libSettings = basicSettings ++ publishingSettings

lazy val root = Project("stamina", file("."))
  .enablePlugins(FormattingPlugin)
  .settings(basicSettings: _*)
  .settings(publishingSettings: _*)
  .aggregate(
    core,
    json,
    testkit
  )
  .enablePlugins(ReproducibleBuildsPlugin)

lazy val core = Project("stamina-core", file("stamina-core"))
  .enablePlugins(FormattingPlugin)
  .settings(libSettings: _*)
  .settings(
    libraryDependencies ++= compileDeps(pekkoActor) ++ testDeps(scalatest)
  )
  .enablePlugins(ReproducibleBuildsPlugin)

lazy val json = Project("stamina-json", file("stamina-json"))
  .enablePlugins(FormattingPlugin)
  .dependsOn(core)
  .settings(libSettings: _*)
  .settings(
    libraryDependencies ++=
      compileDeps(sprayJson) ++
      testDeps(
        scalatest,
        jsonLenses
      )
  )
  .enablePlugins(ReproducibleBuildsPlugin)

lazy val testkit = Project("stamina-testkit", file("stamina-testkit"))
  .enablePlugins(FormattingPlugin)
  .dependsOn(core)
  .settings(libSettings: _*)
  .settings(libraryDependencies ++= compileDeps(scalatest))
  .enablePlugins(ReproducibleBuildsPlugin)
