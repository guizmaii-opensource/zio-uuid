import BuildHelper.{noDoc, scala3, stdSettings}

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion      := scala3
ThisBuild / scalafmtCheck     := true
ThisBuild / scalafmtSbtCheck  := true
ThisBuild / scalafmtOnCompile := !insideCI.value
ThisBuild / scalafixOnCompile := !insideCI.value
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision // use Scalafix compatible version

// ### Aliases ###

addCommandAlias("tc", "Test/compile")
addCommandAlias("ctc", "clean; tc")
addCommandAlias("rctc", "reload; ctc")
addCommandAlias("fix", "scalafixAll; scalafmtAll; scalafmtSbt")
addCommandAlias("check", "scalafixAll --check; scalafmtCheckAll; scalafmtSbtCheck")

// ### Dependencies ###

lazy val zioVersion = "2.1.26"

// ### Modules ###

lazy val root =
  project
    .in(file("."))
    .settings(noDoc *)
    .settings(
      name               := "zio-uuid",
      publish / skip     := true,
      crossScalaVersions := Nil, // https://www.scala-sbt.org/1.x/docs/Cross-Build.html#Cross+building+a+project+statefully
    )
    .aggregate(
      `zio-uuid`
    )

lazy val `zio-uuid` =
  project
    .in(file("zio-uuid"))
    .settings(stdSettings *)
    .settings(
      name               := "zio-uuid",
      crossScalaVersions := Seq(scala3),
      libraryDependencies ++= Seq(
        "dev.zio"            %% "zio"         % zioVersion,
        "dev.zio"            %% "zio-prelude" % "1.0.0-RC48",
        "dev.zio"            %% "zio-json"    % "1.0.0"    % Optional,
        "dev.zio"            %% "zio-test"    % zioVersion % Test,
        "org.scalameta"      %% "munit"       % "1.3.6"    % Test,
        "com.github.poslegm" %% "munit-zio"   % "0.4.1"    % Test,
      ),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    )

inThisBuild(
  List(
    organization             := "com.guizmaii",
    homepage                 := Some(url("https://github.com/guizmaii-opensource/zio-uuid")),
    licenses                 := List("Apache 2.0" -> url("https://opensource.org/license/apache-2.0")),
    Test / parallelExecution := false,
    Test / fork              := true,
    run / fork               := true,
    scalafixDependencies ++= List(
      "com.github.vovapolu"                      %% "scaluzzi" % "0.1.23",
      "io.github.ghostbuster91.scalafix-unified" %% "unified"  % "0.0.9",
    ),
    developers               := List(
      Developer(
        "ant8e",
        "Antoine Comte",
        "",
        url("https://github.com/ant8e"),
      ),
      Developer(
        "guizmaii",
        "Jules Ivanic",
        "",
        url("https://github.com/guizmaii"),
      ),
    ),
  )
)
