scalaVersion := scalaV.v3

scalafmtOnCompile := true

Compile / compile := ((Compile / compile) dependsOn (Compile / scalafmtSbt)).value

libraryDependencies ++= libScalax.`cats-effect`.value
libraryDependencies ++= libScalax.`cats-effect`.value
libraryDependencies ++= libScalax.`javet`.value
libraryDependencies ++= libScalax.`jintellitype`.value
libraryDependencies ++= libScalax.`jnativehook`.value
libraryDependencies ++= libScalax.`logback-classic`.value
libraryDependencies ++= libScalax.`fs2`.value

val pekkoVersion     = "1.0.1"
val pekkoHttpVersion = "1.0.0"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed"           % pekkoVersion,
  "org.apache.pekko" %% "pekko-stream-typed"          % pekkoVersion,
  "org.apache.pekko" %% "pekko-http"                  % pekkoHttpVersion,
  "org.apache.pekko" %% "pekko-http-spray-json"       % pekkoHttpVersion,
  "org.apache.pekko" %% "pekko-serialization-jackson" % pekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j"                 % pekkoVersion
)

addCommandAlias("r1", "reStart")

Global / onChangedBuildSource := ReloadOnSourceChanges
