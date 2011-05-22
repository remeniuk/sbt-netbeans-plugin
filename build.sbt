organization := "org.netbeans"

name := "sbt-netbeans-plugin"

version := "0.0.8_0.9.7"

sbtPlugin := true

resolvers += ScalaToolsSnapshots

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0-SNAPSHOT"

publishArtifact in (Compile, packageDoc) := false

publishMavenStyle := true

publishTo := Some(Resolver.file("ghrepo", new File("/home/remeniuv/remeniuk.github.com/maven"))(Patterns(true, Resolver.mavenStyleBasePattern)))
