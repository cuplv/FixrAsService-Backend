package edu.colorado.plv.fixr
import java.io.{File, IOException, PrintWriter}
import java.net._

import org.eclipse.jgit.api._
import org.eclipse.jgit.lib.TextProgressMonitor

import sys.process._

/**
  * Created by chihwei on 4/24/17.
  */
class BuilderService {

  def cloneRemoteRepository(user: String, repo: String): Option[String] = {

    val url = "https://github.com/" + user + "/" + repo

    if(isRepoValid(url)) {

      // prepare a new folder for the cloned repository
      val currentDir = System.getProperty("user.dir")
      val dir: File = new File(currentDir)

      val localPath: File = File.createTempFile(repo, "", dir)
      println("Temp file : " + localPath.getAbsolutePath())

      if (!localPath.delete()) {
        throw new IOException("Could not delete temporary file " + localPath)
      }

      // then clone
      println("Cloning from " + url + " to " + localPath)

      try {
        val result: Git = Git.cloneRepository().setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out))).setURI(url).setDirectory(localPath).call()
        println("Having repository: " + result.getRepository().getDirectory())
        result.close()
        Some(localPath.getAbsolutePath())
      } catch {
        case ex: Exception => println(ex)
          None
      }
    }else{None}
  }

  def executeGradle(path : String) : Boolean = {

    try {
      val output = Seq("gradle", "-p", path, "assembleDebug").!!
      println(output)
      if(output.indexOf("FAILED") > -1) {
        false
      }
      true
    } catch {
      case ex: Exception => { ex.printStackTrace(); ex.toString() }
        false
    }
  }

  def isRepoValid(url: String): Boolean = {
    //println("connecting github..." + url)
    HttpURLConnection.setFollowRedirects(false)
    val con: HttpURLConnection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    con.setRequestMethod("HEAD")
    return (con.getResponseCode == HttpURLConnection.HTTP_OK)
  }
}
