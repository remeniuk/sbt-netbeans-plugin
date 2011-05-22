package netbeans.project

import java.nio.charset.Charset
import java.util.Properties
import sbt._
import java.io.File

case class ProjectProperties(originalFilePath: File) 
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
    
  private def subprojectProperties = {
    import scalaz.Scalaz._
    
    project.uses.flatMap{subProject =>
      ((Keys.artifactPath in (subProject, Keys.makePom) get extracted.structure.data) |@|
       (Keys.baseDirectory in subProject get extracted.structure.data)) { (_dest, _baseDirectory) =>
        "reference." + subProject.project + ".jar" -> _dest.absolutePath.replaceAll("\\.pom", ".jar") :: 
        "project." + subProject.project -> _baseDirectory  ::        
        Nil
      } getOrElse Nil
    }
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
    "main.class" -> mainClass.getOrElse(""),
    "dist.jar" -> destPath(extracted.currentRef).getOrElse("")
  ) ++ subprojectProperties

  def store(outputFile: File): Unit =
    IO.writer(outputFile.asFile, "", Charset.defaultCharset, false) { content =>
      properties.foreach(prop => setProperty(prop._1, prop._2.toString))
      store(content, "Generated with sbt-netbeans-plugin")
    }   
}
