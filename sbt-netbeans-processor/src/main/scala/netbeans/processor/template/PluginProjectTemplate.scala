package netbeans.processor.template

import sbt._

/**
 * Template of the default SBT plugin project with Netbeans support
 */
class PluginProjectTemplate(project: Project) extends DefaultProjectTemplate(project){
  
  override val projectDefinitionTemplate = "/template/project/PluginProject.mustache"
  
}
