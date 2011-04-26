/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netbeans.project

import sbt._

object NetbeansProject {
  
  implicit def toNetbeansProject(projectDefinition: BasicScalaProject with MavenStyleScalaPaths) = 
    new NetbeansProject(projectDefinition)
  
}

class NetbeansProject(projectDefinition: BasicScalaProject with MavenStyleScalaPaths) {

  /**
   * @return true, is the project is a sub-project of another project
   */
  def isSubproject = projectDefinition.info.projectPath != projectDefinition.rootProject.info.projectPath
      
  /**
   * If the project is a sub-project, add prefix to the path
   */
  def projectPathPrefix = 
    if(isSubproject){
      
      /**
       * Recursively builds prefix, with "../"-selector per each level
       */
      def constructPathPrefix(prefix: String, path: Path): String = 
        if(Path.fromFile(path.asFile.getParentFile) != projectDefinition.rootProject.info.projectPath)
          constructPathPrefix("../" + prefix, path) 
      else prefix        
      
      constructPathPrefix("../", projectDefinition.info.projectPath) 
      
    } else ""  
  
}
