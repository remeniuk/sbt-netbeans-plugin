Simple plugin for SBT creating Netbeans project layout, so that SBT project with subprojects and dependencies can be opened in Netbeans. Basic operations, such as `clean`, `compile`, `test`, etc. are supported from UI.

## Running the plugin
Add **sbt-netbeans-plugin** to the plugin configuration of your project (e.g., `project\plugins\Plugins.scala`):

	import sbt._

	class Plugins(info: ProjectInfo) extends PluginDefinition(info) {

          val netbeansPluginRepo = "Netbeans Plugin Github Repo" at "http://remeniuk.github.com/maven/"
          val netbeansPlugin = "org.netbeans.plugin" % "sbt-netbeans-plugin" % "0.0.1"

	}

Mix `org.netbeans.plugins.SbtNetbeansPlugin` into the project definition (`project\build\<project>.scala`):

	import sbt._
	import com.vasilrem.sbtplugins._

	class SampleProject(info: ProjectInfo) extends DefaultWebProject(info) with SbtNetbeansPlugin{	
	   ...
	}

Create Netbeans layout:

        > netbeans-create-profile              
        [info] 
        [info] == netbeans-create-profile ==
        [info] == netbeans-create-profile ==
        [success] Successful.
        [info] 
        [info] Total time: 0 s, completed Apr 20, 2011 11:27:13 PM

Remove Netbeans artifacts from the project folder:

        > netbeans-remove-profile 
        [info] 
        [info] == netbeans-remove-profile ==
        [info] Deleting directory /home/remeniuv/Dropbox/sbt-netbeans-integration/nbproject
        [info] Deleting file /home/remeniuv/Dropbox/sbt-netbeans-integration/build.xml
        [info] == netbeans-remove-profile ==
        [success] Successful.
        [info] 
        [info] Total time: 0 s, completed Apr 21, 2011 12:48:12 AM
