/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netbeans.project

import sbt.Path
import scalaz._
import Scalaz._

trait NetbeansConfigFile {

  val originalFilePath: Path

  def description: String
  
  def validate = (originalFilePath.get.toList match {
      case Nil => originalFilePath.fail
      case head :: tail => this.success
    }).liftFailNel
  
  def store(outputFile: Path = originalFilePath): Unit
    
}
