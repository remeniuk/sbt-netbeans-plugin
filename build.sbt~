organization := "org.netbeans"

name := "sbt-netbeans-plugin"

version := "0.0.10"

sbtPlugin := true

resolvers += ScalaToolsSnapshots

libraryDependencies += "org.scalaz" % "scalaz-core_2.8.1" % "6.0-SNAPSHOT"

publishArtifact in (Compile, packageDoc) := false

publishMavenStyle := true

publishTo := Some(Resolver.file("ghrepo", new File("/home/remeniuv/remeniuk.github.com/maven"))(Patterns(true, Resolver.mavenStyleBasePattern)))

pomPostProcess := { (pom: scala.xml.Node) => 
  import scala.xml._ 
  import scala.xml.transform._ 
  val rewriteRule = new RewriteRule { 
    override def transform(n: Node) = n match { 
      case elem @ Elem(_, "dependency", _, _, _*) if ((elem \ 
"groupId").text == "org.scala-tools.sbt") && ((elem \ "artifactId").text 
startsWith "sbt_")  => NodeSeq.Empty 
      case other => other 
    } 
  } 
  new RuleTransformer(rewriteRule)(pom) 
} 