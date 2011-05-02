organization := "org.netbeans"

name := "sbt-netbeans-plugin"

version := "0.0.6_0.9.5"

sbtPlugin := true

resolvers += ScalaToolsSnapshots

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0-SNAPSHOT"

publishArtifact in (Compile, packageDoc) := false
