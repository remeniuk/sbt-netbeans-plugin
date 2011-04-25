package netbeans.processor.template

import sbt._

/**
 * Template of the default SBT processor project with Netbeans support
 */
class ProcessorProjectTemplate(project: Project) extends DefaultProjectTemplate(project){
  
  override val projectDefinitionTemplate = "/template/project/ProcessorProject.mustache"
  
}
