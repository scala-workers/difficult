import djx.sbt.depts.plugins.{PluginsCollection => pluginCol}

addSbtPlugin(pluginCol.`sbt-scalafmt`)
addSbtPlugin(pluginCol.`sbt-revolver`)
addSbtPlugin(pluginCol.`sbt-twirl`)
addSbtPlugin(pluginCol.`sbt-sonatype`)
addSbtPlugin(pluginCol.`sbt-pgp`)
addSbtPlugin(pluginCol.`sbt-scalajs-crossproject`)
addSbtPlugin(pluginCol.`sbt-git`)
addSbtPlugin(pluginCol.`sbt-scalajs`)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
