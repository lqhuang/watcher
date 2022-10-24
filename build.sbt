val tapirVersion = "1.1.3"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "watcher",
    version := "0.1.0-SNAPSHOT",
    organization := "com.superbloch",
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "org.http4s" %% "http4s-blaze-server" % "0.23.12",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala" % tapirVersion,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.17.5",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.17.5",
      "ch.qos.logback" % "logback-classic" % "1.4.4",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "com.softwaremill.sttp.client3" %% "jsoniter" % "3.8.3" % Test
    )
  )
)
