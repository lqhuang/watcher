import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

// val cpsVersion      = "0.4.0"
val ceVersion       = "3.5.4"
val log4catsVersion = "2.6.0"
val http4sVersion   = "0.23.25"
val blazeVersion    = "0.23.16"
val circeVersion    = "0.14.6"
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
  "-Ysafe-init",
  // "-Yexplicit-nulls",
  // "-Ymacro-annotations",
  // "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  // "-Ywarn-dead-code", // Warn when dead code is identified.
  // "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
)

ThisBuild / tlBaseVersion     := "0.1"
ThisBuild / semanticdbEnabled := true
ThisBuild / scalafixDependencies += "org.typelevel" %% "typelevel-scalafix" % "0.1.5"

val dependencies = Seq(
  "org.typelevel" %% "cats-effect-std"     % ceVersion,
  "org.http4s"    %% "http4s-dsl"          % http4sVersion,
  "org.http4s"    %% "http4s-blaze-server" % blazeVersion,
  "org.http4s"    %% "http4s-circe"        % http4sVersion,
  "io.circe"      %% "circe-core"          % circeVersion,
  "io.circe"      %% "circe-generic"       % circeVersion,
  "io.circe"      %% "circe-parser"        % circeVersion,
  "org.typelevel" %% "log4cats-core"       % log4catsVersion,
  "org.typelevel" %% "log4cats-slf4j"      % log4catsVersion,
  "ch.qos.logback" % "logback-classic"     % "1.5.3",
  // "org.typelevel" %%% "log4cats-js-console" % log4catsVersion,
  // "org.typelevel" %% "cats-effect-cps"     % cpsVersion,
  // "org.typelevel" %% "cats-time" % "0.5.1",
  // "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % tapirVersion,
  // "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  // "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs"     % tapirVersion,
  "org.scalameta"       %% "munit"                      % munitVersion % Test,
  "org.http4s"          %% "http4s-blaze-client"        % blazeVersion % Test,
  "org.typelevel"       %% "cats-effect"                % ceVersion    % Test,
  "com.alejandrohdezma" %% "http4s-munit"               % "0.15.1"     % Test,
  "com.dimafeng"        %% "testcontainers-scala-munit" % "0.41.3"     % Test,
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
      name         := "watcher",
      packageName  := "watcher",
      version      := "0.1.5-SNAPSHOT",
      maintainer   := "lqhuang@outlook.com",
      organization := "io.lqhuang",
      scalaVersion := "3.3.3",
      libraryDependencies ++= dependencies,
      headerLicense       := Some(headerLic),
      headerEmptyLine     := false,
      Compile / mainClass := Some("io.lqhuang.watcher.Main"),
      /**
       * In order to generate launch scripts only for specified `mainClass`, you
       * will need to discard automatically found main classes:
       */
      // Compile / discoveredMainClasses := Seq(),
      /**
       * sbt-native-packager settings
       */
      // Universal
      Universal / name        := name.value,
      Universal / packageName := packageName.value,
      Universal / javaOptions ++= Seq(
        "-Dlogback.configurationFile=${app_home}/../conf/logback.xml",
      ),
      Universal / mappings += file("README.md") -> "README.md",
      // GraalVM
      graalVMNativeImageOptions ++= Seq(
        "--static", // creates a statically linked executable that has no dependencies on external libraries
        "--no-fallback", // fail the build instead of generating fallback code when native image cannot resolve uses of reflection or other issues
        "--verbose",
      ),
      // Docker
      Docker / packageName                 := packageName.value,
      Docker / version                     := version.value,
      Docker / maintainer                  := "Lanqing Huang",
      Docker / defaultLinuxInstallLocation := "/app",
      Docker / daemonUser                  := "worker",
      Docker / daemonUserUid               := Some("1000"),
      dockerBaseImage          := "eclipse-temurin:21.0.1_12-jre-alpine",
      dockerExposedPorts       := Seq(8080),
      dockerPermissionStrategy := DockerPermissionStrategy.CopyChown,
      dockerBuildInit := false, // docker buildx has no `--init` flag (already enabled by default)
      dockerRepository := Some("ghcr.io"),
      dockerUsername   := Some("lqhuang"),
      dockerLabels := Map(
        "org.opencontainers.image.source" -> "https://github.com/lqhuang/watcher"
      )
    )
  )
