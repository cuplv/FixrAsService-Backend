package edu.colorado.plv.fixr

import java.io.IOException

import com.typesafe.config.ConfigFactory
import edu.colorado.plv.fixr.FixrServer.getClass
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import sys.process._
import scalaj.http._

/**
  * Created by chihwei on 4/19/17.
  */
class GroumsService {

  val config = ConfigFactory.load("application.conf")

  // call by compute method route
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

    //get groum output
    val groumOutput = queryGroumBlackBox(user, repo, method, hash)
    val result_code = (groumOutput \ "result_code").as[Int]

    if(result_code != 0){
      //fail
      groumOutput
    }else{

      var patternsList :ListBuffer[JsObject] = new ListBuffer[JsObject]()

      val weights = (groumOutput \ "patterns" \\ "obj_val")
      val keys = (groumOutput \ "patterns" \\ "pattern_key")

      //get weight and id, and use id to query Solr for groum pattern
      (weights zip keys).foreach{
        case (weight, key) =>
          try{
            //get meta data from solr
            val patternStr = qeurySolrById(key.as[String])
            val patternJson = (Json.parse(patternStr) \ "doc").as[JsValue]

            //not sure if i need to reorganize groum/groumKey in the "pattern", or just include the raw data from solr
            //Yue told me to use the format from Solr, so I set the result from Solr as "pattern"
            val pattern: JsObject = Json.obj("weight" -> weight, "pattern" -> patternJson)
            patternsList += pattern
          }catch {
            case ex: IOException =>
          }
      }

      //get cluster_id
      val cluster_id = (patternsList(0) \ "pattern" \\ "cluster_key_sni")

      val result: JsObject = Json.obj("cluster_id" -> cluster_id(0), "patterns" -> patternsList)
      result.as[JsValue]

    }

  }

  //call by query provenance route
  def queryProvenance(user: String, repo: String, className: String, method: String, hash: String): JsValue ={
    val result: JsObject = Json.obj("groum" -> "some form of representation", "githubLink" -> "path/path")
    result.as[JsValue]
  }

  def queryGroumBlackBox(user: String, repo: String, method: String, hash: Option[String]): JsValue={

    val FixrGraphPatternSearch = config.getString("fixr.groums.FixrGraphPatternSearch")
    val test_env_graph = config.getString("fixr.groums.test_env") + "graphs/"
    val test_env_cluster = config.getString("fixr.groums.test_env") + "clusters"
    val FixrGraphIso = config.getString("fixr.groums.FixrGraphIso")
    var result = ""

    //run Groum search command line
    hash match {
      case Some(hashcommit) =>
        result = Seq("python", FixrGraphPatternSearch, "-d", test_env_graph, "-u", user, "-r", repo, "-z", hashcommit, "-m", method, "-c", test_env_cluster, "-i", FixrGraphIso).!!
      case None =>
        result = Seq("python", FixrGraphPatternSearch, "-d", test_env_graph, "-u", user, "-r", repo, "-m", method, "-c", test_env_cluster, "-i", FixrGraphIso).!!
    }

    println(Json.prettyPrint(Json.parse(result)))
    Json.parse(result)
  }

  def qeurySolrById(id: String): String = {
    try {
      val url = config.getString("fixr.groums.solrURL")
      val response: HttpResponse[String] = Http(url).param("id", id).timeout(connTimeoutMs = 1000, readTimeoutMs = 5000).asString
      response.body
    } catch {
      case ex: IOException => ex.asInstanceOf[String]
    }
  }


}
