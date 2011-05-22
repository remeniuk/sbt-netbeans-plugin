package netbeans.plugin

import sbt._
import FileUtilities._
import Path._
import project._
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
   * Creates files required by Netbeans for an existing SBT project
   */
  lazy val netbeansCreateFiles = task {
    (rootProject.path(".") / "project" / "plugins" ** "*.jar")
    .filter(_.relativePath.contains("sbt-netbeans-plugin")).get map copyNetbeansFiles           
    None
  }
  
  /**
   * Creates files required by Netbeans and updates them with SBT
   * settings
   */
  lazy val netbeansCreateProfile = super.updateAction
  .dependsOn(netbeansUpdateConfig
             .dependsOn(netbeansUpdateDependencies
                        .dependsOn(netbeansCreateFiles)))

  /**
   * Updates Netbeans project profile/config according to SBT properties. Project 
   * config should normally be updated only, if a new subproject has been
   * added
   */  
  lazy val netbeansUpdateConfig = task {
    AntScript("build.xml", this, log).store
    NetbeansProjectConfiguration("nbproject" / "project.xml", this, log).store    
    None
  }
  
  /**
   * Updates all Netbeans files
   */  
  lazy val netbeansUpdateProfile = netbeansUpdateConfig dependsOn netbeansUpdateDependencies
  
  /**
   * Updates Netbeans properties according to SBT properties. Properties should
   * be updated every time a new dependency is added to the SBT
   */  
  lazy val netbeansUpdateDependencies = task {
    NetbeansProjectProperties("nbproject" / "project.properties", this, log).store
    None
  }  

  /**
   * Removes Netbeans artifacts from the project
   */
   lazy val netbeansRemoveProfile = task {
      FileUtilities.clean((path("build.xml") +++ path("nbproject")).get, log)
      None
    }

   /**
    * By default, Netbeans project/config files are updated every time `update`
    * task is executed.
    * The method should be overriden (to return `false`), if refresh of the 
    * Netbeans project files should not happen on each SBT update
    */
   def refreshNetbeansOnUpdate = true
    
   /**
    * Updates Netbeans project files on SBT `update`, if required 
    */
   override def updateAction = 
     if(refreshNetbeansOnUpdate) 
       netbeansUpdateDependencies.dependsOn(super.updateAction) 
   else update
  
   }