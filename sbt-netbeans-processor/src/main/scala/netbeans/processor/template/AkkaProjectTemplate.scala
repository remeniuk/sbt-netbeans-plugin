package netbeans.processor.template

import sbt._

/**
 * Template of the default Akka project with Netbeans support
 */
class AkkaProjectTemplate(project: Project) extends DefaultProjectTemplate(project){

  override val pluginsDefinitionTemplate = "/template/plugin/AkkaPlugins.scala"    
  override val projectDefinitionTemplate = "/template/project/DefaultAkkaProject.mustache"    
  
  /**
   * Path to the Akka config template
   */
  val akkaConfTemplate = "/template/akka/akka.conf"
  
  /**
   * Akka config location
   */  
  val akkaConfLocation = project.path("src") / "main" / "resources" / "akka.conf"    
      
  /**
   * Creates Akka configuration files: akka.conf
   */
  private def createAkkaConf =     
    readFromClasspath(akkaConfTemplate)
  .fold(exception => throw new Exception(exception),
        template => writeFile(template, akkaConfLocation)) 
  
  /**
   * Creates required files of the SBT project with Netbeans support
   */
  override def createProjectArtifacts = {
    super.createProjectArtifacts
    createAkkaConf
  }  
  
}
