organization := "org.netbeans"

name := "sbt-netbeans-plugin"

version := "0.1.3"

sbtPlugin := true

resolvers ++= Seq(ScalaToolsSnapshots, "Typesafe Repo" at "http://repo.typesafe.com/typesafe")

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.3"

publishArtifact in (Compile, packageDoc) := false

publishMavenStyle := true

publishTo := Some(Resolver.file("ghrepo", new File("/home/remeniuv/projects/remeniuk.github.com/maven"))(Patterns(true, Resolver.mavenStyleBasePattern)))
