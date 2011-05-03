package netbeans.project

import java.nio.charset.Charset
import java.util.Properties
import sbt._

case class ProjectProperties(originalFilePath: Path) 
(implicit context: ProjectContext)
extends Properties with NetbeansConfigFile{

  import context._
  import src._
  import res._
  
  val description = "project properties (nbproject/project.properties)"
  
  val extracted = Project.extract(currentState)
  
  IO.reader(originalFilePath, Charset.defaultCharset)(load)
    
  protected def destPath(projectRef: ProjectRef) = 
    (Keys.artifactPath in (projectRef, Keys.makePom) get extracted.structure.data)
  .map(_.absolutePath.replaceAll("\\.pom", ".jar"))
  
  private def subprojectProperties =
    project.uses.flatMap{subProject =>
      "project." + subProject.project -> subProject.project  ::
      "reference." + subProject.project + ".jar" -> destPath(subProject).getOrElse("") :: 
      Nil
    }
      
  def properties = Map(
    "application.title" -> name,
    "file.reference.main-scala" -> scalaSource,    
    "file.reference.test-scala" -> scalaTestSource,
    "file.reference.main-java" -> javaSource,    
    "file.reference.test-java" -> javaTestSource,    
    "test.resources.dir" -> testResources,    
    "resources.dir" -> resources,                
    "javac.classpath" -> compileClasspathString,
    "javac.test.classpath" -> testClasspathString,
    "dist.jar" -> destPath(extracted.currentRef).getOrElse("")
  ) ++ subprojectProperties

  def store(outputFile: Path): Unit =
    IO.writer(outputFile.asFile, "", Charset.defaultCharset, false) { content =>
      properties.foreach(prop => setProperty(prop._1, prop._2.toString))
      store(content, "Generated with sbt-netbeans-plugin")
    }   
}
