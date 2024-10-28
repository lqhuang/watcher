import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

val ceVersion       = "3.5.5"
val log4catsVersion = "2.7.0"
val http4sVersion   = "0.23.29"
val blazeVersion    = "0.23.16"
val circeVersion    = "0.14.10"
val jsoniterVersion = "2.17.5"
val munitVersion    = "1.0.2"

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
ThisBuild / startYear         := Some(2024)
ThisBuild / developers        := List(tlGitHubDev("lqhuang", "Lanqing Huang"))
ThisBuild / scalafixDependencies += "org.typelevel" %% "typelevel-scalafix" % "0.3.1"

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
  "com.outr"      %% "scribe"              % "3.15.2",
  // "org.typelevel" %% "cats-effect-cps"     % cpsVersion,
  // "org.typelevel" %% "cats-time" % "0.5.1",
  "org.scalameta"       %% "munit"                      % munitVersion % Test,
  "org.typelevel"       %% "munit-cats-effect"          % "2.0.0"      % Test,
  "org.typelevel"       %% "cats-effect"                % ceVersion    % Test,
  "org.http4s"          %% "http4s-blaze-client"        % blazeVersion % Test,
  "com.alejandrohdezma" %% "http4s-munit"               % "1.1.0"      % Test,
  "com.dimafeng"        %% "testcontainers-scala-munit" % "0.41.4"     % Test,
)

val headerLic = HeaderLicense.ALv2(
  "2024",
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
      scalaVersion := "3.5.2",
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
