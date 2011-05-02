/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netbeans

import sbt.complete.DefaultParsers._

object NetbeansCommands {
  
  import NetbeansPlugin._
  
  val updateOptions = "update" ~> token(Space  ~> ("all" ^^^ updateNetbeansFiles _)) 
  val netbeansConsole = Space  ~> token("create" ^^^ createNetbeansFiles _ | 
                                        updateOptions | 
                                        "remove" ^^^ removeNetbeansFiles _)   

}
