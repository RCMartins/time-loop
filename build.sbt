name := "time-loop"

inThisBuild(
  Seq(
    organization := "io.github.rcmartins",
    scalaVersion := "2.13.17",
  )
)

val V = new {
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
        "dev.zio"                    %%% "zio-json"  % "0.7.44",
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
  val targetFile = baseDirectory.value / copyPath / "time-loop-scala.js"
  if (frontendJSFile.hashString != targetFile.hashString) {
    IO.copyFile(frontendJSFile, targetFile)
    IO.copyFile(
      frontendJSFile.getParentFile / "time-loop-fastopt.js.map",
      baseDirectory.value / copyPath / "time-loop-fastopt.js.map"
    )
  }
}

fullOptCompileCopy := {
  val frontendJSFile = (frontend / copyFrontendFullOpt).value
  val targetFile = baseDirectory.value / copyPath / "time-loop-scala.js"
  if (frontendJSFile.hashString != targetFile.hashString) {
    IO.copyFile(frontendJSFile, targetFile)
    IO.copyFile(
      frontendJSFile.getParentFile / "time-loop-fastopt.js.map",
      baseDirectory.value / copyPath / "time-loop-fastopt.js.map"
    )
  }
}

addCommandAlias("c", "fastOptCompileCopy")

Global / onChangedBuildSource := ReloadOnSourceChanges
