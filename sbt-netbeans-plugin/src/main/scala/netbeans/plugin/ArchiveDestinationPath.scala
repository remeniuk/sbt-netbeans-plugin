/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package netbeans.plugin

import sbt._

/**
 * Destination path of a project archive (jar/war)
 */
trait ArchiveDestinationPath {
  def destPath: Path
}

case class ProjectDestPath(project: ScalaPaths) extends ArchiveDestinationPath{
  def destPath: Path = project.jarPath
}

case class WebProjectDestPath(project: WebScalaPaths) extends ArchiveDestinationPath{
  def destPath: Path = project.warPath
}

object ArchiveDestinationPath {
  implicit def getDestPath(project: ScalaPaths): ArchiveDestinationPath = project match {
    case webProject:WebScalaPaths => WebProjectDestPath(webProject)
    case other => ProjectDestPath(project)
  }
}
