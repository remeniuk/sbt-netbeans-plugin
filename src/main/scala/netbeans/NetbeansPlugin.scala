package netbeans

import sbt._
import Keys._
import project._
import CommandSupport._

object NetbeansPlugin extends Plugin {
  
  private val buildXmlTemplateLocation = BuildPaths.defaultGlobalPlugins / "src" / "main" / "resources" / "build.xml"
  private val projectFilesTemplateLocation = BuildPaths.defaultGlobalPlugins / "src" / "main" / "resources" / "nbproject"
  
  private def copyNetbeansFiles(basePath: Path)(pluginJarPath: File) = 
    IO.unzip(pluginJarPath, (basePath / "test").asFile, "*.xml" | "*.properties") 
  
  private def copyPackedTemplates(libClasspath: Classpath, dest: Path) = {
    libClasspath.filter(_.data.getName.contains("sbt-netbeans-plugin"))      
    .headOption.map(_.data).map(copyNetbeansFiles(dest))                  
  }  
    
  private def copyUnpackedTemplates(dest: Path) = {
    buildXmlTemplateLocation.get.map { template =>
      IO.copy(Seq(template.asFile -> (dest / "build.xml").asFile), false)  
    }
    projectFilesTemplateLocation.get.map { template =>
      IO.copyDirectory(template.asFile, (dest / "nbproject").asFile, false)  
    }    
  }
      
  override lazy val settings = Seq(commands += netbeansCommands)
  
  private[netbeans] def updateNetbeansFiles(projectRef: ProjectRef, 
                                            s: State, 
                                            projectFiles: Seq[ProjectContext => NetbeansConfigFile]) = {

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
  }

  private[netbeans] def updateAll(projectRef: ProjectRef, s: State) = 
    updateNetbeansFiles(projectRef, s, 
                        Seq((context => AntScript(context.baseDirectory / "build.xml")(context)), 
                            (context => ProjectConfiguration(context.baseDirectory / "nbproject" /"project.xml")(context)),
                            (context => ProjectProperties(context.baseDirectory / "nbproject" / "project.properties")(context)))
    )
  
  private[netbeans] def updateProjectProperties(projectRef: ProjectRef, s: State) = 
    updateNetbeansFiles(projectRef, s, Seq((context => ProjectConfiguration(context.baseDirectory / "nbproject" /"project.xml")(context))))

  private[netbeans] def updateProjectConfig(projectRef: ProjectRef, s: State) = 
    updateNetbeansFiles(projectRef, s, Seq((context => ProjectConfiguration(context.baseDirectory / "nbproject" /"project.xml")(context))))
  
  private[netbeans] def createSourceDirectories(projectRef: ProjectRef, s: State) =         
    ProjectContext.netbeansContext(projectRef, s).map{context =>
      logger(s).info("Creating empty source directories for project `%s`..." format(projectRef.project))
      
      IO.createDirectories(Seq(
          context.baseDirectory / "src" / "main" / "scala",
          context.baseDirectory / "src" / "test" / "scala",
          context.baseDirectory / "src" / "main" / "resources",
          context.baseDirectory / "src" / "test" / "resources"
        ))
    } 
    
  private[netbeans] def createNetbeansFiles(projectRef: ProjectRef, s: State) = {
    val extracted = Project extract s
    import extracted._
    
    logger(s).info("Creating Netbeans files for project `%s`" format(projectRef.project))
    for{base <- baseDirectory in (projectRef, Compile) get structure.data
        classpath <- Project.evaluateTask(fullClasspath in (projectRef, Compile), s)}{
      ProjectContext.toOption(classpath).map(copyPackedTemplates(_, base))
      copyUnpackedTemplates(base)
    }
  }
  
  private[netbeans] def removeNetbeansFiles(projectRef: ProjectRef, s: State) = {
    val extracted = Project extract s
    import extracted._
    
    logger(s).info("Removing Netbeans files from project `%s`" format(projectRef.project))
    (baseDirectory in (projectRef, Compile) get structure.data) map { base =>
      IO.delete((base / "build.xml" +++ base / "nbproject").get)
    }
  }
    
  type NetbeansCommand = (ProjectRef, State) => Any
  
  lazy val netbeansCommands = 
    Command("netbeans")(_ => NetbeansCommands.netbeansConsole) { (state: State, output: Any) =>
      output match {
        case (cmd: NetbeansCommand, transitive: Boolean) => 
          val extracted = Project extract state
          cmd(extracted.currentRef, state)
          if(transitive){
            logger(state).info("Executing command transitively...")
            extracted.currentProject.uses.foreach(cmd(_, state))
          }
          state
        case _ =>  
          logger(state).error("Failed to process command line!")
          state.fail   
      }
    }
  
}
