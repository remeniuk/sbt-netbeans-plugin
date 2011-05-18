package netbeans.project

import scala.xml.{XML, Node, NodeSeq, Elem, UnprefixedAttribute, Null}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import sbt._
import ProjectContext._
import java.io.File

case class ProjectConfiguration(originalFilePath: File)(implicit context: ProjectContext) extends NetbeansConfigFile{
    
  val description = "project configuration (nbproject/project.xml)"
              
  private val sourceRoots = {
    import context._
    import src._
    import res._
            
    Map(
      ("src.dir", "Scala Source") -> scalaSource.isEmpty,
      ("src.java.dir", "Java Source") -> javaSource.isEmpty,
      ("resources.dir", "Resources") -> resources.isEmpty,
      ("sources.sbt.project", "Configuration") -> false
    ).filter(_._2 == false).keys
  }
  
  private val testRoots = {
    import context._
    import src._
    import res._
    
    Map(
      ("test.src.dir", "Scala Test Sources") -> scalaTestSource.isEmpty,
      ("file.reference.test-java", "Java Test Sources") -> javaTestSource.isEmpty,
      ("test.resources.dir", "Test Resources") -> testResources.isEmpty
    ).filter(_._2 == false).keys
  }  
  
  private val projectConfig = XML.loadFile(originalFilePath.asFile)
    
  private val projectConfigRuleTransformer =
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
                       
        override def transform(n: Node): Seq[Node] = 
          n match {
            case <name>{_}</name>  =>
              <name>{context.name}</name>
            
            case Elem(prefix, "references", attribs, scope)  =>
              Elem(prefix, "references", attribs, scope, references:_ *)            
   
            case <source-roots>{_*}</source-roots> =>
              <source-roots>{
                  sourceRoots.flatMap{case (id, name) => <root name={name} id={id}/>}
                }</source-roots> 
              
            case <test-roots>{_*}</test-roots> =>
              <test-roots>{
                  testRoots.flatMap{case (id, name) => <root name={name} id={id}/>}
                }</test-roots>
                          
            case other => other
          }
        
        
      })
  
  def store(outputFile: File): Unit = 
    IO.write(outputFile.asFile, projectConfigRuleTransformer(projectConfig).toString.getBytes)

}
