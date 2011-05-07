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
    
  val create = for{c <- createNetbeansFiles(_); 
                   u <- updateAll(_)} yield c andThen u
  
  val createSourceDirs = for{c <- createSourceDirectories(_); 
                             u <- updateProjectConfig(_)} yield c andThen u
  
  /** If `transitive` is used, command is applied to all projects used by 
   * current project  */
  val transitive = token(Space ~> ("transitive" ^^^ true)) ?? false
  
  val createOptions = "create" ~> (token(Space ~> ("source-directories" ^^^ createSourceDirs)) ?? create)
  
  val updateOptions = "update" ~> (token(Space ~> ("all" ^^^ updateAll _ | 
                                                   "dependencies" ^^^ updateProjectProperties _)) ?? updateAll _) 
      
  val removeOptions = "remove" ^^^ removeNetbeansFiles _
  
  val netbeansConsole = Space  ~> 
  (((createOptions | updateOptions | removeOptions) ~ transitive) map {      
      case (command, transitive) => (command, transitive)
    }) ?? (create, true)

}
