organization := "org.netbeans"

name := "sbt-netbeans-plugin"

version := "0.1.0"

sbtPlugin := true

resolvers += ScalaToolsSnapshots

libraryDependencies += "org.scalaz" % "scalaz-core_2.8.1" % "6.0-SNAPSHOT"

publishArtifact in (Compile, packageDoc) := false

publishMavenStyle := true

publishTo := Some(Resolver.file("ghrepo", new File("/home/remeniuv/remeniuk.github.com/maven"))(Patterns(true, Resolver.mavenStyleBasePattern)))
