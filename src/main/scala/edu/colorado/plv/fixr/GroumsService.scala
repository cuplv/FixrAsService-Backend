package edu.colorado.plv.fixr

import play.api.libs.json._
import sys.process._

/**
  * Created by chihwei on 4/19/17.
  */
class GroumsService {

  def searchGroums(user: String, repo: String, className: String, method: String, hash: Option[String]): JsValue ={
    //calling builder
    /*val githubRepo = new BuilderService().cloneRemoteRepository(user, repo)
    githubRepo match {
      case Some(path) => println(path)
        val isbuilt = new BuilderService().executeGradle(path)
        println(isbuilt)
      case None => println("fail to clone github repository")
    }
    */


    var groumKey: JsObject = Json.obj()
    hash match {
      case Some(hashcommit) => groumKey = Json.obj("user" -> user, "repo" -> repo, "class" -> className, "method" -> method, "hash" -> hashcommit)
      case None => groumKey = Json.obj("user" -> user, "repo" -> repo, "class" -> className, "method" -> method, "hash" -> "NONE")
    }

    val currentDir = System.getProperty("user.dir")
    val path = currentDir+"/src/main/resources/isol_1.dot"
    val output = Seq("cat", path).!!
    //println(output)
    val groumPat: JsObject = Json.obj("groum" -> output, "provenance" -> List(groumKey, groumKey))
    val pattern: JsObject = Json.obj("weight" -> 10, "pattern" -> List(groumPat, groumPat))


    var result: JsObject = Json.obj("cluster_id" -> 1, "patterns" -> List(pattern, pattern))
    result.as[JsValue]
  }

  def queryProvenance(user: String, repo: String, className: String, method: String, hash: String): JsValue ={
    val result: JsObject = Json.obj("groum" -> "some form of representation", "githubLink" -> "path/path")
    result.as[JsValue]
  }


}
