scalaVersion := scalaV.v3

scalafmtOnCompile := true

Compile / compile := ((Compile / compile) dependsOn (Compile / scalafmtSbt)).value

libraryDependencies ++= libScalax.`cats-effect`.value
libraryDependencies ++= libScalax.`cats-effect`.value
libraryDependencies ++= libScalax.`javet`.value
libraryDependencies ++= Seq("com.melloware" % "jintellitype" % "1.4.1")

addCommandAlias("r1", "reStart")

Global / onChangedBuildSource := ReloadOnSourceChanges
