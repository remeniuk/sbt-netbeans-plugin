/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netbeans.project

import sbt.Path

trait NetbeansConfigFile {

  val originalFilePath: Path
  
  def store(outputFile: Path = originalFilePath): Unit
  
}
