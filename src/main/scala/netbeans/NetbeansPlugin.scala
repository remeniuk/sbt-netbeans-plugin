package netbeans

import sbt._
import Keys._
import project._

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

  private[netbeans] def updateNetbeansFiles(projectRef: ProjectRef, state: State) = {
    ProjectContext.netbeansContext(projectRef, state).map{implicit context =>
      Seq(
        new AntScript(context.baseDirectory / "build.xml"),
        new ProjectProperties(context.baseDirectory / "nbproject" / "project.properties"),
        new ProjectConfiguration(context.baseDirectory / "nbproject" /"project.xml")
      ) map(_.store())
    }    
  }
  
  private[netbeans] def createNetbeansFiles(projectRef: ProjectRef, s: State) = {
    val extracted = Project extract s
    import extracted._
    
    for{base <- baseDirectory in (projectRef, Compile) get structure.data
        classpath <- Project.evaluateTask(fullClasspath in (projectRef, Compile), s)}{
      ProjectContext.toOption(classpath).map(copyPackedTemplates(_, base))
      copyUnpackedTemplates(base)
    }
  }
  
  private[netbeans] def removeNetbeansFiles(projectRef: ProjectRef, s: State) = {
    val extracted = Project extract s
    import extracted._
    
    (baseDirectory in (projectRef, Compile) get structure.data) map { base =>
      IO.delete((base / "build.xml" +++ 
                 base / "nbproject").get)
    }
  }
    
  type NetbeansCommand = (ProjectRef, State) => Any
  
  lazy val netbeansCommands = 
    Command("netbeans")(_ => NetbeansCommands.netbeansConsole) { (state: State, cmd: NetbeansCommand) => 
      val extracted = Project extract state
      cmd(extracted.currentRef, state)
      extracted.currentProject.uses.foreach(cmd(_, state))
      state
    }
  
}
