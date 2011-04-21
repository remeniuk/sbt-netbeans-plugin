package org.netbeans.plugins

import sbt._
import FileUtilities._
import Path._
import sbtnb._
import java.io.{File, InputStream, OutputStream, FileInputStream, FileOutputStream}
import java.util.Properties
import scala.xml._
import scala.xml.transform._

trait SbtNetbeansPlugin extends BasicScalaProject with MavenStyleScalaPaths {

  /**
   * Copies Netbeans project artifacts from the plugin jar
   */
  private def copyNetbeansFiles(pluginJarPath: Path) = {
    unzip(pluginJarPath.get.toList.head, ".", "*.xml" | "*.properties" , log)
    None
  }

  /**
   * Creates Netbeans profiles for an existing SBT project
   */
  lazy val netbeansCreateProfile = task{

    (rootProject.path(".") / "project" / "plugins" ** "*.jar")
    .filter(_.relativePath.contains("sbt-netbeans-plugin")).get map copyNetbeansFiles

    NetbeansProjectProperties("nbproject" / "project.properties", this, log).store
    AntScript("build.xml", this, log).store
    NetbeansProjectConfiguration("nbproject" / "project.xml", this, log).store

    None
  }

  /**
   * Removes Netbeans artifacts from the project
   */
  lazy val netbeansRemoveProfile = task {
    FileUtilities.clean((path("build.xml") +++ path("nbproject")).get, log)
    None
  }

}