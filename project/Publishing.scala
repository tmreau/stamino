import sbt.Keys._
import sbt._

object Publishing {
  lazy val publishingSettings = Seq(
    homepage := Some(url("https://github.com/makeorg")),
    pomExtra :=
      <developers>
        <developer>
          <id>tmreau</id>
          <name>Thomas Moreau</name>
          <url>https://github.com/makeorg</url>
        </developer>
      </developers>,
  )
}
