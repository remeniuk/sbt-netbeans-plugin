/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {

  val netbeansPluginRepo = "Netbeans Plugin Github Repo" at "http://remeniuk.github.com/maven/"
  val netbeansPlugin = "org.netbeans.plugin" % "sbt-netbeans-plugin" % "0.0.2"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.7"

}