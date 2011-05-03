/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netbeans

import sbt._
import sbt.complete.DefaultParsers._

object NetbeansCommands {
  
  import NetbeansPlugin._ 
  import scalaz._
  import Scalaz._
    
  private val create = createNetbeansFiles _ *> updateAll _
  private val createSourceDirs = createSourceDirectories _ *> updateProjectConfig _
  
  val transitive = token(Space ~> ("transitive" ^^^ true)) ?? false
  
  val createOptions = "create" ~> (token(Space ~> ("source-directories" ^^^ createSourceDirs)) ?? create)
  
  val updateOptions = "update" ~> token(Space ~> ("all" ^^^ updateAll _ | 
                                                  "dependencies" ^^^ updateProjectProperties _)) 
      
  val removeOptions = "remove" ^^^ removeNetbeansFiles _
  
  val netbeansConsole = Space  ~> 
  (token((createOptions | updateOptions | removeOptions) ~ transitive) map {      
      case (command, transitive) => (command, transitive)
    }) ?? (create, false)

}
