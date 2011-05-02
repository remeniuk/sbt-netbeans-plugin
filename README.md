**sbt-netbeans-plugin** is a plugin for simple-build-tool that allows working with SBT projects in Netbeans IDE.

### Building from source

Clone **sbt-netbeans-plugin**:

    $ git clone -n git://github.com/remeniuk/sbt-netbeans-plugin.git
    $ cd sbt-netbeans-plugin

Switch to 0.9 branch:

    $ git checkout 0.9

Publish to the local ivy repository:

    $ xsbt publish-local

### Installing the plugin

You can either add sources of the plugin to `~/.sbt/plugins` or add a managed dependency to the plugin artifact (in the both cases, plugin will be globally available):

    $ cd .sbt/plugins/
    $ xsbt
    > set libraryDependencies += "org.netbeans" %% "sbt-netbeans-plugin" % "0.0.6_0.9.5"
    > session save
    > exit

### Using the plugin

All the commands applied to the project will also be applied to all the projects it depends on or aggregates (e.g., `netbeans create` applied to the root project will create Netbeans files not only for the root, but also for all the dependencies, etc.).

Create Netbeans files:

    > netbeans create

Update Netbeans files with SBT project settings:

    > netbeans update all

Remove Netbeans files:

    > netbeans remove
