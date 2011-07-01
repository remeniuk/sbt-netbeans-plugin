organization := "org.netbeans"

name := "sbt-netbeans-plugin"

version := "0.1.0"

sbtPlugin := true

resolvers ++= Seq(ScalaToolsSnapshots, "Typesafe Repo" at "http://repo.typesafe.com/typesafe")

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.1"

publishArtifact in (Compile, packageDoc) := false

publishMavenStyle := true

publishTo := Some(Resolver.file("ghrepo", new File("/home/remeniuv/remeniuk.github.com/maven"))(Patterns(true, Resolver.mavenStyleBasePattern)))