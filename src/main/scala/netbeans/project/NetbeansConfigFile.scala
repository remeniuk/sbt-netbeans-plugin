/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netbeans.project

import java.io.File
import scalaz._
import Scalaz._
import sbt._

trait NetbeansConfigFile {

  val originalFilePath: File

  def description: String
  
  def validate = (originalFilePath.get.toList match {
      case Nil => originalFilePath.fail
      case head :: tail => this.success
    }).liftFailNel
  
  def store(outputFile: File = originalFilePath): Unit
    
}
