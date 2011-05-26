/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netbeans.project

import java.io.File
import sbt._

object ProjectContext{

  import Keys._
  import scalaz.Scalaz._
  
  class EmptyPath(path: File){
    def isEmpty = path.get.headOption.isEmpty
  }
  
  def toOption[T](res: Result[T]) = res match {
    case Value(r) => Some(r)
    case _ => None  
  }
  
  implicit def fileToEmptyPath(file: File) = new EmptyPath(file)
  
  def projectSources(currentProjectRef: ProjectRef, s: State) = {
    val extracted = Project extract s
    import extracted._    
    
    val _mainClass = Project.evaluateTask(mainClass in (currentProjectRef, Compile, run), s)
    .flatMap(toOption).flatMap(x => x)
        
    (scalaSource in (currentProjectRef, Compile) get structure.data) |@| 
    (scalaSource in (currentProjectRef, Test) get structure.data) |@| 
    (javaSource in (currentProjectRef, Compile) get structure.data) |@| 
    (javaSource in (currentProjectRef, Test) get structure.data) |@|  
    Some(_mainClass) apply ProjectSources.apply
  }
  
  def projectResources(currentProjectRef: ProjectRef, s: State) = {
    val extracted = Project extract s
    import extracted._    
    
    (resourceDirectory in (currentProjectRef, Compile) get structure.data) |@| 
    (resourceDirectory in (currentProjectRef, Test) get structure.data) apply ProjectResources.apply
  }  
      
  def projectClasspaths(currentProjectRef: ProjectRef, s: State) = {
    val extracted = Project extract s
    import extracted._    
    
    ((Project.evaluateTask(externalDependencyClasspath in (currentProjectRef, Compile), s) |@|
      Project.evaluateTask(externalDependencyClasspath in (currentProjectRef, Test), s)) apply { (compCp, testCp) =>
        (toOption(compCp) |@| toOption(testCp)) apply ProjectClasspaths.apply
      }).flatten.headOption
  } 
  
  def netbeansContext(currentProjectRef: ProjectRef, s: State) = {
    val extracted = Project extract s
    import extracted._    
         
    Option(s) |@|
    (thisProject in (currentProjectRef, Compile) get structure.data) |@|
    (name in (currentProjectRef, Compile) get structure.data) |@|
    (baseDirectory in (currentProjectRef, Compile) get structure.data) |@|
    projectSources(currentProjectRef, s) |@|
    projectResources(currentProjectRef, s) |@| 
    projectClasspaths(currentProjectRef, s) |@|
    (scalaInstance in (currentProjectRef, Compile) get structure.data) apply ProjectContext.apply
  }
  
} 

case class ProjectSources(scalaSource: File, scalaTestSource: File, 
                          javaSource: File, javaTestSource: File, mainClass: Option[String])
case class ProjectResources(resources: File, testResources: File)
case class ProjectClasspaths(compileClasspath: Keys.Classpath, testClasspath: Keys.Classpath)

case class ProjectContext(currentState: State, project: ResolvedProject, name: String, 
                          baseDirectory: File, src: ProjectSources, res: ProjectResources,
                          classpaths: ProjectClasspaths, scalaInstance: ScalaInstance){
  
  protected def subprojectClasspath =
    project.uses
  .map(subProject => "${reference.%s.jar}".format(subProject.project))
  
  val sbtJars = (baseDirectory \ "project" ** "sbt" ** "*.jar").get.map(_.absolutePath).toList  
  val scalaJars = scalaInstance.jars
  
  val compileClasspathString = (subprojectClasspath ++ classpaths.compileClasspath.files ++ 
                                scalaJars ++ sbtJars).mkString(":")
  val testClasspathString = (classpaths.testClasspath.files ++ scalaJars).mkString(":")
  
}
