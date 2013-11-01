import java.io.File

import sbt._
import Keys._

object XitrumPlugin extends Plugin {
  // Must be lazy to avoid null error
  // xitrumPackageNeedsPackageBin must be after xitrumPackageTask
  override lazy val settings = Seq(xitrumPackageTask, xitrumPackageNeedsPackageBin)

  //----------------------------------------------------------------------------

  val xitrumPackageKey = TaskKey[Unit]("xitrum-package", "Packages to target/xitrum directory, ready for deploying to production server")

  // Must be lazy to avoid null error
  lazy val xitrumPackageTask = xitrumPackageKey <<=
    // dependencyClasspath: both internalDependencyClasspath and externalDependencyClasspath
    // internalDependencyClasspath ex: classes directories
    // externalDependencyClasspath ex: .jar files
    (dependencyClasspath in Runtime, baseDirectory, target,    crossTarget) map {
    (libs,                           baseDir,       targetDir, jarOutputDir) =>
    try {
      val packageDir = targetDir / "xitrum"
      deleteFileOrDirectory(packageDir)
      packageDir.mkdirs()

      // Copy dependencies to lib directory
      val libDir = packageDir / "lib"
      libs.foreach { lib =>
        val file = lib.data

        if (file.exists) {
          if (file.isDirectory) {
            // This dependency may be "classes" directory from SBT multimodule (multiproject)
            // http://www.scala-sbt.org/0.13.0/docs/Getting-Started/Multi-Project.html
            //
            // Ex:
            // /Users/ngoc/src/xitrum-multimodule-demo/module1/target/scala-2.10/classes
            // /Users/ngoc/src/xitrum-multimodule-demo/module1/target/scala-2.10/xitrum-multimodule-demo-module1_2.10-1.0-SNAPSHOT.jar
            if (file.name == "classes") {
              val upperDir = file / ".."
              (upperDir * "*.jar").get.foreach { f => IO.copyFile(f, libDir / f.name) }
            }
          } else {
            IO.copyFile(file, libDir / file.name)
          }
        }
      }

      // Copy .jar files created after running "sbt package" to lib directory
      // (see xitrumPackageNeedsPackageBin)
      (jarOutputDir * "*.jar").get.foreach { f => IO.copyFile(f, libDir / f.name) }

      // TODO: https://github.com/ngocdaothanh/xitrum-sbt-plugin/issues/1
      val copyFiles = Seq("bin", "config", "public")
      copyFiles.foreach { f => copy(f, baseDir, packageDir) }

      println("Packaged to " + packageDir)
    } catch {
      case e: Exception => e.printStackTrace
    }
  }

  val xitrumPackageNeedsPackageBin = xitrumPackageKey <<= xitrumPackageKey.dependsOn(packageBin in Compile)

  //----------------------------------------------------------------------------

  private def deleteFileOrDirectory(file: File) {
    if (file.isDirectory) {
      val files = file.listFiles
      if (files != null) files.foreach { f => deleteFileOrDirectory(f) }
    }
    file.delete()
  }

  private def copy(fileName: String, baseDir: File, packageDir: File) {
    val from = baseDir / fileName
    if (!from.exists) return

    val to = packageDir / fileName
    if (from.isDirectory) IO.copyDirectory(from, to) else IO.copyFile(from, to)

    // TODO: keep executable property (note: there may be subdirs)
    //val files = to.listFiles
    //if (files != null) files.foreach { _.setExecutable(true) }
  }
}
