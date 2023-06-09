import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

// val cpsVersion      = "0.4.0"
val ceVersion       = "3.5.1"
val log4catsVersion = "2.6.0"
val http4sVersion   = "0.23.22"
val blazeVersion    = "0.23.15"
val circeVersion    = "0.14.5"
val jsoniterVersion = "2.17.5"
val munitVersion    = "0.7.29"
// val tapirVersion    = "1.1.3"

Global / onChangedBuildSource := ReloadOnSourceChanges
Compile / run / fork          := true

scalacOptions ++= Seq(
  // "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  // "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  // "-language:strictEquality",
  "-explain", // Explain errors in more detail.
  "-Yexplicit-nulls",
  "-Ysafe-init",
  // "-Ymacro-annotations",
  // "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  // "-Ywarn-dead-code", // Warn when dead code is identified.
  // "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
)

ThisBuild / tlBaseVersion     := "0.1"
ThisBuild / semanticdbEnabled := true
ThisBuild / scalafixDependencies += "org.typelevel" %% "typelevel-scalafix" % "0.1.5"

val dependencies = Seq(
  // "org.typelevel" %% "cats-effect-cps"     % cpsVersion,
  "org.typelevel" %% "cats-effect-std"     % ceVersion,
  "org.typelevel" %% "log4cats-slf4j"      % log4catsVersion,
  "org.http4s"    %% "http4s-dsl"          % http4sVersion,
  "org.http4s"    %% "http4s-blaze-server" % blazeVersion,
  "org.http4s"    %% "http4s-circe"        % http4sVersion,
  "io.circe"      %% "circe-core"          % circeVersion,
  "io.circe"      %% "circe-generic"       % circeVersion,
  "io.circe"      %% "circe-parser"        % circeVersion,
  "ch.qos.logback" % "logback-classic"     % "1.4.8",
  // "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % tapirVersion,
  // "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  // "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs"     % tapirVersion,
  "org.scalameta"       %% "munit"                      % munitVersion % Test,
  "org.http4s"          %% "http4s-blaze-client"        % blazeVersion % Test,
  "org.typelevel"       %% "cats-effect"                % ceVersion    % Test,
  "com.alejandrohdezma" %% "http4s-munit"               % "0.15.0"     % Test,
  "com.dimafeng"        %% "testcontainers-scala-munit" % "0.40.17"    % Test,
  // "org.typelevel"       %% "munit-cats-effect"   % "v2.0.0-M3"  % Test,
)

val headerLic = HeaderLicense.ALv2(
  "2023",
  "Lanqing Huang",
  HeaderLicenseStyle.SpdxSyntax
)

// .enablePlugins(StainlessPlugin)
lazy val rootProject = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin) // For alpine ash script
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    Seq(
      name            := "watcher",
      packageName     := "watcher",
      version         := "0.1.4-SNAPSHOT",
      organization    := "io.lqhuang",
      headerLicense   := Some(headerLic),
      headerEmptyLine := false,
      scalaVersion    := "3.3.0",
      libraryDependencies ++= dependencies,
      Compile / mainClass := Some("io.lqhuang.watcher.Main"),
      /**
       * In order to generate launch scripts only for specified `mainClass`, you
       * will need to discard automatically found main classes:
       */
      // Compile / discoveredMainClasses := Seq(),
      graalVMNativeImageOptions ++= Seq(
        "--static", // creates a statically linked executable that has no dependencies on external libraries
        "--no-fallback", // fail the build instead of generating fallback code when native image cannot resolve uses of reflection or other issues
        "--verbose",
      ),
      // Universal
      Universal / name        := name.value,
      Universal / packageName := packageName.value,
      // Universal / javaOptions ++= Seq("-Dlogback.level=info"),
      // Docker
      Docker / packageName                 := packageName.value,
      Docker / version                     := version.value,
      Docker / maintainer                  := "Lanqing Huang",
      Docker / defaultLinuxInstallLocation := "/app",
      dockerBaseImage                      := "eclipse-temurin:17.0.5_8-jre",
      dockerExposedPorts                   := Seq(8080),
      dockerPermissionStrategy := DockerPermissionStrategy.CopyChown,
      dockerBuildInit          := true,
      dockerRepository         := Some("ghcr.io"),
      dockerUsername           := Some("lqhuang"),
      dockerLabels := Map(
        "org.opencontainers.image.source" -> "https://github.com/lqhuang/watcher"
      )
    )
  )
