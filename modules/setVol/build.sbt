scalaVersion := scalaV.v3

crossScalaVersions := List(scalaV.v212, scalaV.v213, scalaV.v3)

scalafmtOnCompile := true

Compile / compile := ((Compile / compile) dependsOn (Compile / scalafmtSbt)).value

libraryDependencies ++= libScalax.`cats-effect`.value
libraryDependencies ++= libScalax.`javet`.value
libraryDependencies ++= libScalax.`logback-classic`.value
libraryDependencies ++= libScalax.`fs2`.value
libraryDependencies ++= libScalax.`better-monadic-for`.value
libraryDependencies ++= libScalax.`scala-collection-compat`.value
