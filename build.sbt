scalaVersion := scalaV.v3

scalafmtOnCompile := true

Compile / compile := ((Compile / compile) dependsOn (Compile / scalafmtSbt)).value

libraryDependencies ++= libScalax.`cats-effect`.value
libraryDependencies ++= libScalax.`cats-effect`.value
libraryDependencies ++= libScalax.`javet`.value

addCommandAlias("r1", "reStart")

Global / onChangedBuildSource := ReloadOnSourceChanges
