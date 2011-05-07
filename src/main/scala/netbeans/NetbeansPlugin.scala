package netbeans

import sbt._
import Keys._
import project._
import CommandSupport._

object NetbeansPlugin extends Plugin {
  
  /** build.xml template under ./sbt/plugins (overrides the template in plugin jar) */
  private val buildXmlTemplateLocation = BuildPaths.defaultGlobalPlugins / "src" / "main" / "resources" / "build.xml"
  /** project.properties template under ./sbt/plugins (overrides the template in plugin jar) */
  private val projectFilesTemplateLocation = BuildPaths.defaultGlobalPlugins / "src" / "main" / "resources" / "nbproject"
  
  /** Extracts Netbeans project files templates from the plugin jar */
  private def copyNetbeansFiles(basePath: Path)(pluginJarPath: File) = 
    IO.unzip(pluginJarPath, (basePath / "test").asFile, "*.xml" | "*.properties") 
  
  /** Copies packed Netbeans project files templates to the project folder */
  private def copyPackedTemplates(libClasspath: Classpath, dest: Path) = {
    libClasspath.filter(_.data.getName.contains("sbt-netbeans-plugin"))      
    .headOption.map(_.data).map(copyNetbeansFiles(dest))                  
  }  
    
  /** Copies Netbeans project files templates from ./sbt/plugins */
  private def copyUnpackedTemplates(dest: Path) = {
    buildXmlTemplateLocation.get.map { template =>
      IO.copy(Seq(template.asFile -> (dest / "build.xml").asFile), false)  
    }
    projectFilesTemplateLocation.get.map { template =>
      IO.copyDirectory(template.asFile, (dest / "nbproject").asFile, false)  
    }    
  }
  
  /** Adds sbt-netbeans commands globally */
  override lazy val settings = Seq(commands += netbeansCommands)
  
  /** Updates Netbeans project files with SBT project settings */
  private[netbeans] def updateNetbeansFiles(projectRef: ProjectRef, 
                                            s: State, 
                                            projectFiles: Seq[ProjectContext => NetbeansConfigFile]): State = {

    import scalaz.Scalaz._
    
    logger(s).info("Updating Netbeans files for project `%s`..." format(projectRef.project))    
    
    ProjectContext.netbeansContext(projectRef, s).map{context =>
      projectFiles.map(_(context)).foreach{ projectFile =>
        projectFile.validate.fold ({ errors => 
            logger(s).error("%s: failed to update %s"
                            .format(projectRef.project, projectFile.description))        
          }, { _ => 
            projectFile.store()
            logger(s).info("%s: successfully updated %s"
                           .format(projectRef.project, projectFile.description))
          }        
        )
      }
    } 
    
    s
  }
        
  /** Updates all Netbeans project files */
  private[netbeans] def updateAll(projectRef: ProjectRef)(s: State): State = 
    updateNetbeansFiles(projectRef, s, 
                        Seq((context => AntScript(context.baseDirectory / "build.xml")(context)), 
                            (context => ProjectConfiguration(context.baseDirectory / "nbproject" /"project.xml")(context)),
                            (context => ProjectProperties(context.baseDirectory / "nbproject" / "project.properties")(context)))
    )
    
  /** Updates project.properties */
  private[netbeans] def updateProjectProperties(projectRef: ProjectRef)(s: State): State = 
    updateNetbeansFiles(projectRef, s, Seq((context => ProjectProperties(context.baseDirectory / "nbproject" /"project.properties")(context))))

  /** Updates project.xml */
  private[netbeans] def updateProjectConfig(projectRef: ProjectRef)(s: State): State = 
    updateNetbeansFiles(projectRef, s, Seq((context => ProjectConfiguration(context.baseDirectory / "nbproject" /"project.xml")(context))))
  
  /** Creates empty source/resource directories */
  private[netbeans] def createSourceDirectories(projectRef: ProjectRef)(s: State): State = {        
    ProjectContext.netbeansContext(projectRef, s).map{context =>
      logger(s).info("Creating empty source directories for project `%s`..." format(projectRef.project))
      
      IO.createDirectories(Seq(
          context.baseDirectory / "src" / "main" / "scala",
          context.baseDirectory / "src" / "test" / "scala",
          context.baseDirectory / "src" / "main" / "resources",
          context.baseDirectory / "src" / "test" / "resources"
        ))            
    }
    s
  } 
   
  /** Adds Netbeans project files to the SBT project */
  private[netbeans] def createNetbeansFiles(projectRef: ProjectRef)(s: State): State = {
    val extracted = Project extract s
    import extracted._
    
    logger(s).info("Creating Netbeans files for project `%s`" format(projectRef.project))
    for{base <- baseDirectory in (projectRef, Compile) get structure.data
        classpath <- Project.evaluateTask(fullClasspath in (projectRef, Compile), s)}{
      ProjectContext.toOption(classpath).map(copyPackedTemplates(_, base))
      copyUnpackedTemplates(base)
    }
    
    if(session.original.filter(_.key.key.label == NetbeansTasks.updateDepTaskKey).isEmpty){
      logger(s).info("Writing plugin settings for project `%s`" format(projectRef.project))
      NetbeansTasks.writePluginSettings(projectRef, structure)    
      s.reload
    } else s
  }
  
  /** Removes Netbeans project files from the SBT project */
  private[netbeans] def removeNetbeansFiles(projectRef: ProjectRef)(s: State): State = {
    val extracted = Project extract s
    import extracted._
    
    logger(s).info("Removing Netbeans files from project `%s`" format(projectRef.project))
    (baseDirectory in (projectRef, Compile) get structure.data) map { base =>
      IO.delete((base / "build.xml" +++ base / "nbproject").get)
    }
    
    s
  }
    
  type NetbeansCommand = ProjectRef => State => State
    
  lazy val netbeansCommands = 
    Command("netbeans")(_ => NetbeansCommands.netbeansConsole) { (state: State, output: Any) =>
      output match {
        case (cmd: NetbeansCommand, transitive: Boolean) => 
          val extracted = Project extract state
          val s = cmd(extracted.currentRef)(state)
          if(transitive){
            logger(state).info("Executing command transitively...")
            (s /: extracted.currentProject.uses) {(_s, _ref) => cmd(_ref)(_s)}
          } else s
        case other =>  
          logger(state).error("Failed to process command line: " + other)
          state.fail   
      }
    }
  
}
