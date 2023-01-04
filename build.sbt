val cpsVersion      = "0.4.0"
val http4sVersion   = "0.23.12"
val blazeVersion    = http4sVersion
val circeVersion    = "0.14.1"
val tapirVersion    = "1.1.3"
val jsoniterVersion = "2.17.5"

Global / onChangedBuildSource := ReloadOnSourceChanges

scalacOptions ++= Seq(
  // "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  // "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-explain", // Explain errors in more detail.
  "-Yexplicit-nulls",
  // "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  // "-Ywarn-dead-code", // Warn when dead code is identified.
  // "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
)

ThisBuild / tlBaseVersion     := "0.1"
ThisBuild / semanticdbEnabled := true

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name         := "watcher",
    version      := "0.1.0-SNAPSHOT",
    organization := "io.lqhuang",
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
      "org.typelevel"               %% "cats-effect-cps"     % cpsVersion,
      "org.http4s"                  %% "http4s-dsl"          % http4sVersion,
      "org.http4s"                  %% "http4s-blaze-server" % blazeVersion,
      "io.circe"                    %% "circe-core"          % circeVersion,
      "io.circe"                    %% "circe-generic"       % circeVersion,
      "io.circe"                    %% "circe-parser"        % circeVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs"     % tapirVersion,
      "ch.qos.logback"              %% "logback-classic"         % "1.4.4",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
    )
  )
)
