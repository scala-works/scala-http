ThisBuild / scalaVersion := "3.2.1"
ThisBuild / organization := "works.scala"

val tapirVersion = "1.2.6"

lazy val server = project
  .in(file("server"))
  .settings(
    fork := true,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core"              % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server"      % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
    ),
  )

lazy val example = project
  .in(file("example"))
  .dependsOn(server)
