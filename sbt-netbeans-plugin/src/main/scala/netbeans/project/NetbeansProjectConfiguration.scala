package netbeans.project

import scala.xml.{XML, Node, NodeSeq, Elem, Text}
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
          case <name>{_}</name>  =>
            <name>{projectDefinition.projectName.value}</name>
            
          case Elem(prefix, "references", attribs, scope)  =>
            Elem(prefix, "references", attribs, scope, references:_ *)
   
          case elem @ Elem(_, "root", attributes, _, children @ _*) 
            if((elem \\ "@id").text match {
                case "src.dir" if(projectDefinition
                                  .mainScalaSourcePath.get.isEmpty) => true
                case "resources.dir" if(projectDefinition
                                        .mainResourcesPath.get.isEmpty) => true
                case "test.src.dir" if(projectDefinition
                                       .testScalaSourcePath.get.isEmpty) => true
                case "test.resources.dir" if(projectDefinition
                                             .testResourcesPath.get.isEmpty) => true
                case _ => false
              }) => NodeSeq.Empty 
            
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
