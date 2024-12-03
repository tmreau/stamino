import sbt._

object Dependencies {
  // format: OFF
  val pekkoActor  = "org.apache.pekko" %% "pekko-actor"  % "1.1.2"
  val sprayJson  = "io.spray"          %% "spray-json"  % "1.3.6"
  val jsonLenses = "net.virtual-void"  %% "json-lenses" % "0.6.2"
  val scalatest  = "org.scalatest"     %% "scalatest"   % "3.2.19"
  val circe = Seq("circe-generic", "circe-parser").map("io.circe" %% _ % "0.14.10")
  // format: ON

  // Dependency scoping functions
  def compileDeps(deps: ModuleID*): Seq[ModuleID] = deps.map(_ % Compile)
  def testDeps(deps: ModuleID*): Seq[ModuleID] = deps.map(_ % Test)
  def providedDeps(deps: ModuleID*): Seq[ModuleID] = deps.map(_ % Provided)
}
