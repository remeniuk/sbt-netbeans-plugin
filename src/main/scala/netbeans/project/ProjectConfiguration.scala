package netbeans.project

import scala.xml.{XML, Node, NodeSeq, Elem}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import sbt._
import ProjectContext._

class ProjectConfiguration(val originalFilePath: Path)(implicit context: ProjectContext) extends NetbeansConfigFile{
      
  private val projectConfig = XML.loadFile(originalFilePath.asFile)

  private  val projectConfigRuleTransformer =
    new RuleTransformer(new RewriteRule {
        val references = context.project.uses.flatMap(subProject =>
          <reference>
            <foreign-project>{subProject.project}</foreign-project>
            <artifact-type>jar</artifact-type>
            <script>build.xml</script>
            <target>jar</target>
            <clean-target>clean</clean-target>
            <id>jar</id>
          </reference>
        ).toSeq
                
        override def transform(n: Node): Seq[Node] = {
          import context._
          import src._
          import res._
          
          n match {
            case <name>{_}</name>  =>
              <name>{context.name}</name>
            
            case Elem(prefix, "references", attribs, scope)  =>
              Elem(prefix, "references", attribs, scope, references:_ *)            
   
            case elem @ Elem(_, "root", attributes, _, children @ _*) 
            
              if((elem \\ "@id").text match {
                  case "src.dir" if(scalaSource.isEmpty) => true
                  case "test.src.dir" if(scalaTestSource.isEmpty) => true
                  case "src.java.dir" if(javaSource.isEmpty) => true
                  case "file.reference.test-java" if(javaSource.isEmpty) => true                                                                  
                  case "resources.dir" if(resources.isEmpty) => true
                  case "test.resources.dir" if(testResources.isEmpty) => true
                  case _ => false
                }) => NodeSeq.Empty 
            
            case other => other
          }
        }
        
      })
  
  def store(outputFile: Path): Unit =
    IO.write(outputFile.asFile, projectConfigRuleTransformer(projectConfig).toString.getBytes)

}
