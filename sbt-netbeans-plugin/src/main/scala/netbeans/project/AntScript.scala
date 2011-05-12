package netbeans.project

import scala.xml.{XML, Node, Elem, Text}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import sbt._
import FileUtilities._
import Path._
import java.io.File

object AntScript{

  def apply(scriptPath: Path,
            projectDefinition: BasicScalaProject with MavenStyleScalaPaths,
            log: Logger) =
              new AntScript(scriptPath, projectDefinition, log)

}

/**
 * Netbeans uses Ant as a default build tool for Scala projects.
 * Ant script template (build.xml) is merged with SBT project 
 * properties, so that Netbeans treats SBT project as a Scala/Ant project
 * @param scriptPath path to Ant script
 * @param projectDefinition SBT project definition
 * @param log logger
 */
class AntScript(scriptPath: Path,
                projectDefinition: BasicScalaProject with MavenStyleScalaPaths,
                log: Logger){

  import AntScript._
  import NetbeansProject._

  /**
   * Operating system name
   */
  lazy val operatingSystem = System.getProperty("os.name")

  /**
   * Transformer rewrites Ant script with SBT project properties
   * and system settings
   */
  private val scriptRewriter = new RuleTransformer(new RewriteRule {

      override def transform(n: Node): Seq[Node] = n match {
        case <project>{children @ _*}</project> =>
          <project name={projectDefinition.projectName.value} default="default" basedir=".">{
              children
            }</project>
          
        case <exec>{args @ _*}</exec> =>
          <exec dir={projectDefinition.projectPathPrefix} 
            executable={
              if(operatingSystem.startsWith("Windows")) "sbt.bat" else "sbt"
            }>{args}</exec>      
          
        case arg @ <arg/> if(projectDefinition.isSubproject) =>
          <arg value={
              ";project %s ;%s".format(projectDefinition.name, (arg \\ "@value").text)
            }/>

        case other => other
      }

    })

  /**
   * Ant script XML loaded from the file
   */
  private val antScript = readString(scriptPath.asFile, log)
  .fold(exception => throw new Exception(exception),
        scriptContents => scriptRewriter(XML.loadString(scriptContents)))

  /**
   * Rewrites script to the location, from where the script was loaded
   */
  def store: Unit = store(scriptPath)

  /**
   * Rewrites script to the specified location
   * @param outputFile script will be written to the specified path
   */
  def store(outputFile: Path): Unit =
    write(outputFile.asFile, antScript.toString.getBytes,log)

}
