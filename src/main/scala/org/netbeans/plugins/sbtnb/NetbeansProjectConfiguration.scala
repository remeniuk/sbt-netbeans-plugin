/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.plugins.sbtnb

import scala.xml.{XML, Node, Elem, Text}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import sbt._
import FileUtilities._
import Path._
import java.io.File

object NetbeansProjectConfiguration{

  def apply(projectConfigPath: Path,
            projectDefinition: BasicScalaProject with MavenStyleScalaPaths,
            log: Logger) = 
              new NetbeansProjectConfiguration(projectConfigPath, projectDefinition, log)

}

/**
 * Netbeans project configuration (nbproject/project.xml)
 * @param projectConfigPath path to Netbeans project configuration
 * @param projectDefinition SBT project definition
 * @param log logger
 */
class NetbeansProjectConfiguration(projectConfigPath: Path,
                                   projectDefinition: BasicScalaProject with MavenStyleScalaPaths,
                                   log: Logger){

  /**
   * Project config XML loaded from file
   */
  private val projectConfig = readString(projectConfigPath.asFile, log)
  .fold(exception => throw new Exception(exception),
        buildFileContents => XML.loadString(buildFileContents))

  /**
   * Transformer rewrites project configuration with SBT project properties
   */
  private  val projectConfigRuleTransformer =
    new RuleTransformer(new RewriteRule {
        val references = projectDefinition.subProjects.flatMap{case (_, subProject) =>
            <reference>
              <foreign-project>{subProject.name}</foreign-project>
              <artifact-type>jar</artifact-type>
              <script>build.xml</script>
              <target>jar</target>
              <clean-target>clean</clean-target>
              <id>jar</id>
            </reference>
        }.toSeq

        override def transform(n: Node): Seq[Node] = n match {
          case Elem(prefix, "name", attribs, scope, _*)  =>
            Elem(prefix, "name", attribs, scope, Text(projectDefinition.projectName.value))
          case Elem(prefix, "references", attribs, scope, _*)  =>
            Elem(prefix, "references", attribs, scope, references:_ *)
          case other => other
        }
      })

  /**
   * Writes config to the location, from where the config was loaded
   */
  def store: Unit = store(projectConfigPath)

  /**
   * Writes config to the specified location
   * @param outputFile config will be written to the specified path
   */
  def store(outputFile: Path): Unit =
    write(outputFile.asFile, projectConfigRuleTransformer(projectConfig).toString.getBytes,log)

}
