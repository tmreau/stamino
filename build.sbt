import Dependencies._
import Publishing._

lazy val scala2 = "2.13.14"
//lazy val scala3 = "3.5.1"
lazy val supportedScalaVersions = List(scala2 /*, scala3*/ )

lazy val basicSettings = Seq(
  organization       := "org.make",
  licenses           := Seq("The MIT License (MIT)" -> url("http://opensource.org/licenses/MIT")),
  scalaVersion       := scala2,
  crossScalaVersions := supportedScalaVersions,
  scalacOptions := Seq(
    "-encoding",
    "utf8",
    "-target:jvm-1.8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-Xlint",
    "-Xlog-reflective-calls",
    "-Ywarn-unused",
  ),
  scalacOptions += {
    if (scalaBinaryVersion.value == "2.13") {
      "-Wunused:imports"
    } else {
      "-Ywarn-unused-import"
    }
  },
)

lazy val libSettings = basicSettings ++ publishingSettings

lazy val root = Project("stamino", file("."))
  .settings(basicSettings: _*)
  .settings(publishingSettings: _*)
  .aggregate(core, json, testkit)
  .enablePlugins(ReproducibleBuildsPlugin)

lazy val core = Project("stamino-core", file("stamino-core"))
  .settings(libSettings: _*)
  .settings(libraryDependencies ++= compileDeps(pekkoActor) ++ testDeps(scalatest))
  .enablePlugins(ReproducibleBuildsPlugin)

lazy val json = Project("stamino-json", file("stamino-json"))
  .dependsOn(core)
  .settings(libSettings: _*)
  .settings(
    libraryDependencies ++=
      compileDeps(sprayJson) ++
        testDeps(scalatest, jsonLenses),
  )
  .enablePlugins(ReproducibleBuildsPlugin)

lazy val testkit = Project("stamino-testkit", file("stamino-testkit"))
  .dependsOn(core)
  .settings(libSettings: _*)
  .settings(libraryDependencies ++= compileDeps(scalatest))
  .enablePlugins(ReproducibleBuildsPlugin)
