**sbt-netbeans** is the set of SBT processor and plugin that make possible using SBT projects in Netbeans. **sbt-netbeans-processor** allows you to create an empty SBT project with Netbeans support in just one move. With **sbt-netbeans-plugin** you can easily *netbeanize* your existing projects. All the sweet spots, such as code-completion, syntax highlighting, sub-projects and dependencies support, and running actions from within the IDE are available.

## History

**Release 0.0.5**:

* Fixed #5, #7;
* Netbeans config files with classpaths are updated every time SBT `update` is called;
* `target`, `plugin`, `boot`, etc. are not displayed at the project pane;

**Release 0.0.4**:

* Added **sbt-netbeans-processor**;
* Simplified package structure - **sbt-netbeans-plugin** sources were moved to `netbeans.plugin`;

**Release 0.0.3**:

* SBT executable in the Ant script is chosen with regards to the current OS;
* Unmanaged dependencies of the plugin projects are correctly recognized;
* `scala-library.jar` and `scala-compiler.jar` are added to the Netbeans project libs to enable highlighting;

**Release 0.0.2**:

* SBT project files, resource/test resources are displayed at the project pane;

## Using the processor
Add the processor's repository:

        > *nbrepo at http://remeniuk.github.com/maven/

Add **sbt-netbeans-processor**:

        > *netbeans is org.netbeans.plugin sbt-netbeans-processor 0.0.5

Create an empty SBT-Netbeans project with just one command (plugins and project definitions will be created for you, and **sbt-netbeans-plugin** will be downloaded and wired automatically):

        > netbeans create default

To get the list of all supported project templates, submit the following command:

       > netbeans create
       [error] Invalid arg string! Use one of the following:
       [error] create default | web | plugin | processor

Processors are added per SBT-user, so once you install **sbt-netbeans-processor**, it will always require just one SBT command to "netbeanize" the project (without a need to manually create or copy any files).
In order to remove or update **sbt-netbeans-processor**, use the following command:

      *remove netbeans

## Running the plugin
**NOTE:** When you create a project using the processor, steps 1-3 are made automatically!

**1.** Add **sbt-netbeans-plugin** to the plugin configuration of your project (e.g., `project\plugins\Plugins.scala`):

	import sbt._

	class Plugins(info: ProjectInfo) extends PluginDefinition(info) {

          val netbeansPluginRepo = "Netbeans Plugin Github Repo" at "http://remeniuk.github.com/maven/"
          val netbeansPlugin = "org.netbeans.plugin" % "sbt-netbeans-plugin" % "0.0.5"

	}

**2.** Mix `org.netbeans.plugins.SbtNetbeansPlugin` into the project definition (`project\build\<project>.scala`):

	import sbt._
	import netbeans.plugin._

	class SampleProject(info: ProjectInfo) extends DefaultWebProject(info) with SbtNetbeansPlugin{	
	   ...
	}

**2.1.** If your project has subprojects, SbtNetbeansPlugin should be mixed with all of them:

        class MainProject(info: ProjectInfo) extends DefaultWebProject(info)
                                                with SbtNetbeansPlugin{

          lazy val subProject = project("sub-project", "sub-project", new SubProject(_))

          class SubProject(info: ProjectInfo) extends DefaultWebProject(info)
                                                 with SbtNetbeansPlugin

        }

**3.** Create Netbeans layout:

        > netbeans-create-profile              
        [info] 
        [info] == netbeans-create-profile ==
        [info] == netbeans-create-profile ==
        [success] Successful.
        [info] 
        [info] Total time: 0 s, completed Apr 20, 2011 11:27:13 PM

**Now you can open your SBT project in Netbeans!**

By default, every time SBT `update` task is called, Netbeans project classpaths are updated. To turn this behavior off, you will need to override `refreshNetbeansOnUpdate` in the project definition:

        def refreshNetbeansOnUpdate = false

Hereafter, every time you add a dependency through SBT, you'll have to call `netbeans-update-dependencies` to update the classpaths.
When you add a sub-project, Netbeans configs need to be updated:

        > netbeans-update-profile

In order to remove Netbeans artifacts from the project folder, you may use `netbeans-remove-profile` task: 

        > netbeans-remove-profile 
        [info] 
        [info] == netbeans-remove-profile ==
        [info] Deleting directory /home/remeniuv/Dropbox/sbt-netbeans-integration/nbproject
        [info] Deleting file /home/remeniuv/Dropbox/sbt-netbeans-integration/build.xml
        [info] == netbeans-remove-profile ==
        [success] Successful.
        [info] 
        [info] Total time: 0 s, completed Apr 21, 2011 12:48:12 AM

## Building from sources

        > git clone git://github.com/remeniuk/sbt-netbeans-plugin.git
        > cd sbt-netbeans-plugin
        > sbt publish-local