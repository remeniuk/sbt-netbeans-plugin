import sbt._
import netbeans.plugin._
import processor.{Processor, Success, ProcessorResult}

class SbtNetbeansProject(info: ProjectInfo) extends DefaultProject(info) with SbtNetbeansPlugin
                                               with posterous.Publish {  

  override def managedStyle = ManagedStyle.Maven
  lazy val publishTo = Resolver.file("GitHub", new java.io.File("../remeniuk.github.com/maven/"))
   
  lazy val pluginProject = project("sbt-netbeans-plugin", "sbt-netbeans-plugin", new SbtNetbeansPluginProject(_))
  lazy val processorProject = project("sbt-netbeans-processor", "sbt-netbeans-processor", new SbtNetbeansProcessorProject(_))

  class SbtNetbeansPluginProject(info: ProjectInfo) extends PluginProject(info) with SbtNetbeansPlugin

  class SbtNetbeansProcessorProject(info: ProjectInfo) extends ProcessorProject(info) with SbtNetbeansPlugin
     
}

