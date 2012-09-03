/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netbeans

import sbt._
import Keys._
import java.nio.charset.Charset
import project._
import CommandSupport._
import NetbeansPlugin._

object NetbeansTasks {

  val exportedSettings = Seq("seq(netbeans.NetbeansTasks.netbeansSettings:_*)")

  val updateDepTaskKey = "netbeans-update-dependencies"

  val netbeansUpdateDependencies = TaskKey[Any](updateDepTaskKey)

  val netbeansSettings = Seq(
    sbtExecutable := "sbt",
    netbeansUpdateDependencies <<= updateProjectPropertiesTask triggeredBy(update)
  )

  /** Updates project properties, if project classpath has changed */
  def updateProjectPropertiesTask = (baseDirectory, fullClasspath in Compile, thisProjectRef, state) map { (base, classpath, projectRef, s) =>
    val pluginCachePath = base / "nbproject" / "netbeans.cache"

    s.log.info("Triggered update dependencies task for proect %s" format(projectRef))

    val cachedClasspath = (pluginCachePath.get.map{ path =>
        val classpathCache = IO.readLines(path, Charset.defaultCharset).mkString(":")
        s.log.info("Cache file exists: %s" format(classpathCache))
        classpathCache
      }).headOption.getOrElse("")

    val currentClasspath = classpath.files.mkString(":")

    if(currentClasspath != cachedClasspath){
      s.log.info("Updating classpath: %s" format(currentClasspath))
      IO.write(pluginCachePath, currentClasspath.getBytes)
      updateProjectProperties(projectRef)(s)
    }
  }

  /** Persists plugin settings in SBT project config */
  def writePluginSettings(pref: ProjectRef, structure: sbt.Load.BuildStructure) = {
    val project = Project.getProject(pref, structure).getOrElse(sys.error("Invalid project reference " + pref))
    val appendTo: File = BuildPaths.configurationSources(project.base).headOption.getOrElse(new File(project.base, "build.sbt"))
    val baseAppend = exportedSettings.flatMap("" :: _ :: Nil)
    val adjustedLines = if(appendTo.isFile && !SessionSettings.needsTrailingBlank(IO readLines appendTo) ) baseAppend else "" +: baseAppend
    IO.writeLines(appendTo, adjustedLines, append = true)
  }

}
