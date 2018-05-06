import sbt._
import sbt.Keys._

object Release {
  val settings = Seq(
    pomIncludeRepository := { _ => false },

    licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php")),

    homepage := Some(url("https://github.com/derekjw/treelogger")),

    scmInfo := Some(
      ScmInfo(
        url("https://github.com/derekjw/treelogger"),
        "scm:https://github.com/derekjw/treelogger.git"
      )
    ),

    developers := List(
      Developer(
        id = "derekjw",
        name = "Derek Williams",
        email = "derek@nebvin.ca",
        url = url("https://github.com/derekjw")
      )
    ),

    publishMavenStyle := true,

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },

    publishArtifact in Test := false
  )
}