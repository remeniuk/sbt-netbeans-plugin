package netbeans.processor.template

import sbt._
import FileUtilities._
import java.io.{File, ByteArrayOutputStream}

object DefaultProjectTemplate {  
  
  /**
   * Simplified binding of the mustache-like templates to the values
   */
  def bindTemplate(template: String, bindings: Map[String, String]) = 
    (template /: bindings){ (templ, entry) =>
      templ.replaceAll("\\{\\{%s\\}\\}" format(entry._1), entry._2)
    }
  
}

/**
 * Template of the default SBT project with Netbeans support
 */
class DefaultProjectTemplate(project: Project) {
  
  import DefaultProjectTemplate._
  
  /**
   * Path to the plugins definition template
   */
  val pluginsDefinitionTemplate = "/template/plugin/Plugins.scala"

  /**
   * Path to the project definition template
   */  
  val projectDefinitionTemplate = "/template/project/DefaultProject.mustache"
  
  /**
   * Public name of the project that will be written to the project definition, etc.
   */
  val publicProjectName = project.name.split("[_-]").map(_.capitalize).mkString + "Project"
 
  /**
   * Project definition location
   */
  val projectDefinition = project.path("project") / "build" / "%s.scala".format(publicProjectName)  

  /**
   * Plugins definition location
   */  
  val pluginsDefinition = project.path("project") / "plugins" / "Plugins.scala"  
   
  /**
   * Bindings that should be applied to the project definition template
   */
  private val projectDefinitionBindings = Map("projectName" -> publicProjectName)
  
  /**
   * Reads file fom the classpath/jar 
   * @param relative path to the classpath resouce
   * @return either file contents or throwable
   */
  private def readFromClasspath(file: String): Either[Throwable, String] = {
    val baos = new ByteArrayOutputStream
    try {      
      transfer(getClass.getResourceAsStream(file), baos, project.log)
      Right(new String(baos.toByteArray))
    } catch {case e => Left(e)} 
    finally {
      try{baos.close} catch {case e => project.log.error(e.getMessage)}
    }
  }
  
  /**
   * Writes file contents to the specified destination
   */
  private def writeFile(contents: String, path: Path) = {
    createDirectory(path.asFile.getParentFile, project.log)
    touch(path, project.log)
    write(path.asFile, contents.getBytes, project.log)    
  }
  
  /**
   * Binds plugin definition template to the values, and writes it to the file
   * system
   */
  private def createPluginsDefinition = 
    readFromClasspath(pluginsDefinitionTemplate)
  .fold(exception => throw new Exception(exception),
        writeFile(_, pluginsDefinition)) 

  /**
   * Binds project definition template to the values, and writes it to the file
   * system
   */
  private def createProjectDefinition =     
    readFromClasspath(projectDefinitionTemplate)
  .fold(exception => throw new Exception(exception),
        template => writeFile(bindTemplate(template, projectDefinitionBindings), 
                              projectDefinition)) 
  
  /**
   * Creates required files of the SBT project with Netbeans support
   */
  def createProjectArtifacts = {
    createPluginsDefinition
    createProjectDefinition
  }

}
