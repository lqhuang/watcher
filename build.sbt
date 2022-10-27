val tapirVersion    = "1.1.3"
val blazeVersion    = "0.23.12"
val jsoniterVersion = "2.17.5"

Global / onChangedBuildSource := ReloadOnSourceChanges

scalacOptions ++= Seq(
  "-explain", // Explain errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Yexplicit-nulls",
  // "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  // "-Ywarn-dead-code", // Warn when dead code is identified.
  // "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
)

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name         := "watcher",
    version      := "0.1.0-SNAPSHOT",
    organization := "com.superbloch",
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
      "org.http4s"                  %% "http4s-blaze-server"     % blazeVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs"     % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala"    % tapirVersion,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.4",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest"                 %% "scalatest" % "3.2.14" % Test,
      "com.softwaremill.sttp.client3" %% "jsoniter"  % "3.8.3"  % Test
    )
  )
)
