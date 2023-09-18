import sbt.dependsOn

scalaVersion := scalaV.v211

scalafmtOnCompile := true

Compile / compile := ((Compile / compile) dependsOn (Compile / scalafmtSbt)).value

libraryDependencies ++= libScalax.`cats-effect`.value
libraryDependencies ++= libScalax.`javet`.value
libraryDependencies ++= libScalax.`jintellitype`.value
libraryDependencies ++= libScalax.`jnativehook`.value
libraryDependencies ++= libScalax.`logback-classic`.value
libraryDependencies ++= libScalax.`fs2`.value
// libraryDependencies ++= libScalax.`pekko-all`.value
libraryDependencies ++= libScalax.`better-monadic-for`.value

val setVol = project in file("modules") / "setVol"

val main = project in file(".") dependsOn setVol

addCommandAlias("r1", "main/reStart")

Global / onChangedBuildSource := ReloadOnSourceChanges
