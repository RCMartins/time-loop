name := "time-loop"

inThisBuild(
  Seq(
    organization := "io.github.rcmartins",
    scalaVersion := "2.13.18",
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:implicitConversions",
      "-language:existentials",
      "-language:dynamics",
      "-Xlint:-unused",
      "-Ybackend-parallelism",
      "4",
      "-Ycache-plugin-class-loader:last-modified",
      "-Ycache-macro-class-loader:last-modified",
      "-Xnon-strict-patmat-analysis",
      "-Xlint:-strict-unsealed-patmat",
      "-Wunused:imports",
    ),
  )
)

val V = new {
  val zio = "2.1.23"
  val zioJson = "0.7.45"
  val laminar = "17.2.1"
  val scalajsDom = "2.8.1"
  val quicklens = "1.9.12"
}

lazy val frontend =
  project
    .in(file("."))
    .enablePlugins(ScalaJSPlugin, BuildInfoPlugin)
    .settings(
      scalaJSUseMainModuleInitializer := true,
      copyFrontendFastOpt := {
        (Compile / fastOptJS).value.data
      },
      copyFrontendFullOpt := {
        (Compile / fullOptJS).value.data
      },
      libraryDependencies ++= Seq(
        // Production
        "com.raquo"                  %%% "laminar"   % V.laminar,
        "com.softwaremill.quicklens" %%% "quicklens" % V.quicklens,
        "dev.zio"                    %%% "zio-json"  % V.zioJson,
        // Test
        "dev.zio" %%% "zio-test"          % V.zio % Test,
        "dev.zio" %%% "zio-test-sbt"      % V.zio % Test,
        "dev.zio" %%% "zio-test-magnolia" % V.zio % Test,
      )
    )

val copyFrontendFastOpt = taskKey[File]("Return main process fast compiled file directory.")
lazy val fastOptCompileCopy =
  taskKey[Unit]("Compile and copy paste projects and generate corresponding json file.")

val copyFrontendFullOpt = taskKey[File]("Return main process full compiled file directory.")
lazy val fullOptCompileCopy =
  taskKey[Unit]("Compile and copy paste projects and generate corresponding json file.")

val copyPath: String = "public/js/"

fastOptCompileCopy := {
  val frontendJSFile = (frontend / copyFrontendFastOpt).value
  val targetFile = baseDirectory.value / copyPath / "time-loop.js"
  if (frontendJSFile.hashString != targetFile.hashString) {
    IO.copyFile(frontendJSFile, targetFile)
    IO.copyFile(
      frontendJSFile.getParentFile / "time-loop-fastopt.js.map",
      baseDirectory.value / copyPath / "time-loop.js.map"
    )
  }
}

fullOptCompileCopy := {
  val frontendJSFile = (frontend / copyFrontendFullOpt).value
  val targetFile = baseDirectory.value / copyPath / "time-loop.js"
  if (frontendJSFile.hashString != targetFile.hashString) {
    IO.copyFile(frontendJSFile, targetFile)
    IO.copyFile(
      frontendJSFile.getParentFile / "time-loop-opt.js.map",
      baseDirectory.value / copyPath / "time-loop.js.map"
    )
  }
}

addCommandAlias("c", "fastOptCompileCopy")

Global / onChangedBuildSource := ReloadOnSourceChanges
