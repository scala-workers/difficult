scalaVersion := scalaV.v213

Global / onChangedBuildSource := ReloadOnSourceChanges

scalafmtOnCompile := true

Compile / compile := ((Compile / compile) dependsOn (Compile / scalafmtSbt)).value
