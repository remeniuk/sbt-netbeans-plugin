package netbeans.project

import sbt._
import scala.xml.{Node, XML}
import scala.xml.transform.{RuleTransformer, RewriteRule}

case class AntScript(originalFilePath: Path)(implicit context: ProjectContext) extends NetbeansConfigFile{
    
  val description = "Ant-script (build.xml)"
  
  lazy val operatingSystem = System.getProperty("os.name")

  private val scriptRewriter = new RuleTransformer(new RewriteRule {
      
      import context._
                  
      val executable = if(operatingSystem.startsWith("Windows")) "sbt.bat" else "sbt"
      
      override def transform(n: Node): Seq[Node] = n match {
        case <project>{children @ _*}</project> =>
          <project name={context.name} default="default">{
              children
            }</project>
          
        case <exec>{args @ _*}</exec> =>
          <exec dir={baseDirectory.absolutePath} executable={executable}>{args}</exec>
          
        case arg @ <arg/> =>
          <arg value={
              val changeProjectCommand = ";project %s;".format(project.id)
              val commandLine = (arg \\ "@value").text
              if(!commandLine.startsWith(changeProjectCommand)) changeProjectCommand + commandLine
              else commandLine
            }/>

        case other => other
      }

    })

  def store(outputFile: Path): Unit = {
    val script = scriptRewriter(XML.loadFile(originalFilePath.asFile)) 
    IO.write(outputFile.asFile, script.toString.getBytes)    
  }

}