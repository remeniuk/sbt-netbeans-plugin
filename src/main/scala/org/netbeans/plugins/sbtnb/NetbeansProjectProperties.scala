/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.plugins.sbtnb

import java.util.Properties
import sbt._
import FileUtilities._
import Path._
import java.io.File
import ArchiveDestinationPath._
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
  .mkString(":")

  /**
   * Netbeans project property keys, mapped to SBT project properties
   */
  private def projectProperties = Map(
    "application.title" -> projectDefinition.projectName.value,

    "file.reference.main-scala" -> projectDefinition.mainScalaSourcePath.get
    .map(_.relativePath).mkString(":"),

    "file.reference.test-scala" -> projectDefinition.testScalaSourcePath.get
    .map(_.relativePath).mkString(":"),

    "javac.classpath" ->
    (subprojectClasspath ::
     (projectDefinition.managedClasspath(projectDefinition.config("compile")) +++
      projectDefinition.unmanagedClasspath).get
     .map(_.relativePath).toList).mkString(":"),

    "javac.test.classpath" -> projectDefinition.testClasspath.get
    .map(_.relativePath).mkString(":"),

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
