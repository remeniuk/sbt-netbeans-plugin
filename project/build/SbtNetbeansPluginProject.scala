import sbt._
import org.netbeans.plugins._

class SbtNetbeansPluginProject(info: ProjectInfo) extends PluginProject(info) 
                                                     with SbtNetbeansPlugin with posterous.Publish{

  override def managedStyle = ManagedStyle.Maven
  lazy val publishTo = Resolver.file("GitHub", new java.io.File("../remeniuk.github.com/maven/"))

}
