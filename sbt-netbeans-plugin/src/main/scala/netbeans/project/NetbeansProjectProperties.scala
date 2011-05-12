package netbeans.project

import java.util.Properties
import sbt._
import FileUtilities._
import Path._
import java.io.File
import netbeans.plugin.ArchiveDestinationPath._
import scala.collection.jcl.Conversions._
import java.util.Collections._

object NetbeansProjectProperties {
  def apply(propertiesPath: Path,
            projectDefinition: BasicScalaProject with MavenStyleScalaPaths,
            log: Logger):NetbeansProjectProperties =
              new NetbeansProjectProperties(propertiesPath, projectDefinition, log)
}

/**
 * Properties for project config and Ant script
 * @param propertiesPath path to Netbeans project properties
 * @param projectDefinition SBT project definition
 * @param log logger
 */
class NetbeansProjectProperties(propertiesPath: Path,
                                projectDefinition: BasicScalaProject with MavenStyleScalaPaths,
                                log: Logger) extends Properties{

  import NetbeansProject._
  
  readStream(propertiesPath.asFile, log) { content =>
    load(content)
    None
  }

  /**
   * Map of properties for sub-projects. The following properties must be
   * defined for all sub-projects:
   * project.<sub-project name>=<sub-project name>
   * reference.<sub-project name>.jar=<path to sub-project jar/war>
   */
  private def subprojectProperties =
    projectDefinition.subProjects
  .map(_._2.asInstanceOf[MavenStyleScalaPaths])
  .flatMap{subProject =>
    Path.relativize(projectDefinition.path("."), subProject.destPath).map{ subProjectDestPath =>
      "project." + subProject.name -> subProject.name ::
      "reference." + subProject.name + ".jar" -> subProjectDestPath.relativePath :: Nil
    }.getOrElse(Nil)
  }

  /**
   * Classpath fragment with the references to sub-projects
   */
  private def subprojectClasspath =
    projectDefinition.subProjects
  .map(subProject => "${reference.%s.jar}".format(subProject._2.name))

  /**
   * Scala compiler and library jars, required for code highlighting
   */
  private def scalaJars =
    projectDefinition.buildLibraryJar :: projectDefinition.buildCompilerJar :: Nil

  private def managedDependencies =
    projectDefinition.managedClasspath(projectDefinition.config("compile")).get

  private def unmanagedDependencies =
    projectDefinition.unmanagedClasspath.flatMap(a => a).get
  
  /**
   * Returns path, relativized from root to the current project
   */  
  private def relativizeFromRoot(path: Path) = 
    Path.relativize(projectDefinition.rootProject.path("."), path)
  .map(projectDefinition.projectPathPrefix + _)
    
      
  /**
   * Netbeans project property keys, mapped to SBT project properties
   */
  private def projectProperties = Map(
    "application.title" -> projectDefinition.projectName.value,

    "file.reference.main-scala" -> projectDefinition.mainScalaSourcePath.relativePath,

    "file.reference.test-scala" -> projectDefinition.testScalaSourcePath.relativePath,

    "javac.classpath" ->
    (subprojectClasspath ++
     (managedDependencies ++ unmanagedDependencies ++ scalaJars)
     .flatMap(relativizeFromRoot)).mkString(":"),

    "javac.test.classpath" -> projectDefinition.testClasspath.get
    .flatMap(relativizeFromRoot).mkString(":"),

    "dist.jar" -> projectDefinition.destPath.relativePath
  ) ++ subprojectProperties

  /**
   * Rewrites script to the location, from where the script was loaded
   */
  def store: Unit = store(propertiesPath)

  /**
   * Rewrites script to the specified location
   * @param outputFile script will be written to the specified path
   */
  def store(outputFile: Path): Unit =
    writeStream(outputFile.asFile, log) { content =>
      projectProperties.foreach(prop => setProperty(prop._1, prop._2))
      store(content, "Generated with sbt-netbeans-plugin")
      None
    }
}
