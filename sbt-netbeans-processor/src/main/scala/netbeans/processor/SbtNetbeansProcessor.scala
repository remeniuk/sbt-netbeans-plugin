package netbeans.processor

import sbt._
import template._
import processor.{Processor, ProcessorResult, Reload, Success}

object SbtNetbeansProcessor{
  
  val errorMessage = "Invalid arg string! Use one of the following:"
  val createProjectHint = "create default | web | plugin | processor | akka"
     
}

class SbtNetbeansProcessor extends Processor{

  import SbtNetbeansProcessor._
  
  def apply(label: String, project: Project, onFailure: Option[String], args: String) = {
    
    /**
     * Prints errors to the error log
     */
    def error(errors: String*) = {
      project.log.error(errors.mkString("\r\n"))  
      new Success(project, onFailure)
    }      
    
    /**
     * Returns SBT project template by name
     */
    val templateByName: PartialFunction[String, Option[DefaultProjectTemplate]] = {
      case "default" => Some(new DefaultProjectTemplate(project))
      case "web" => Some(new DefaultWebProjectTemplate(project))
      case "plugin" => Some(new PluginProjectTemplate(project))
      case "processor" => Some(new ProcessorProjectTemplate(project))
      case "akka" => Some(new AkkaProjectTemplate(project))
      case _ => None   
    }    
    
    args.toLowerCase.split("[ ]+") match {

      /**
       * Creates SBT project with Netbeans support
       */      
      case Array("create", templateName) => 
        templateByName(templateName).map{ template =>
          template.createProjectArtifacts
          new Reload("netbeans-create-profile")
        }.getOrElse(error(errorMessage, createProjectHint))                    
      
      case other => error(errorMessage, createProjectHint)
        
    }
    
  }
  
}
