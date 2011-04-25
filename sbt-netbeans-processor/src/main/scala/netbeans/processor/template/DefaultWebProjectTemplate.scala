package netbeans.processor.template

import sbt._

/**
 * Template of the default SBT web project with Netbeans support
 */
class DefaultWebProjectTemplate(project: Project) extends DefaultProjectTemplate(project){

  override val projectDefinitionTemplate = "/template/project/DefaultWebProject.mustache"
  
}
