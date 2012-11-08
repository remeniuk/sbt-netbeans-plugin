organization := "org.netbeans"

name := "sbt-netbeans-plugin"

version := "0.1.5"

sbtPlugin := true

resolvers ++= Seq(ScalaToolsSnapshots, "Typesafe Repo" at "http://repo.typesafe.com/typesafe")

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

publishArtifact in (Compile, packageDoc) := false

publishMavenStyle := true

projectID <<= (organization,moduleName,version,artifacts,crossPaths){ (org,module,version,as,crossEnabled) =>
	ModuleID(org, module, version).cross(crossEnabled).artifacts(as : _*)
}

publishTo := Some(Resolver.file("ghrepo", new File("/home/remeniuv/projects/remeniuk.github.com/maven"))(Patterns(true, Resolver.mavenStyleBasePattern)))
