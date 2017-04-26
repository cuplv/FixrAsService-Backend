package edu.colorado.plv.fixr

import java.io.IOException

import akka.event.Logging
import akka.event.LoggingAdapter

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
  def searchGroums(user: String, repo: String, className: String, method: String, hash: Option[String], logger: LoggingAdapter): JsValue ={

    //calling builder
    /*val githubRepo = new BuilderService().cloneRemoteRepository(user, repo)
    githubRepo match {
      case Some(path) => println(path)
        val isbuilt = new BuilderService().executeGradle(path)
        println(isbuilt)
      case None => println("fail to clone github repository")
    }
    */

    logger.info("Initiating groums search")

    //get groum output
    val groumOutput = queryGroumBlackBox(user, repo, method, hash, logger)
    val result_code = (groumOutput \ "result_code").as[Int]

    logger.info("Groums search completed")

    if(result_code != 0){
      //fail
      return groumOutput
    }else{

      var patternsList :ListBuffer[JsObject] = new ListBuffer[JsObject]()

      val weights = (groumOutput \ "patterns" \\ "obj_val")
      val keys = (groumOutput \ "patterns" \\ "pattern_key")

      //get weight and id, and use id to query Solr for groum pattern
      (weights zip keys).foreach{
        case (weight, key) =>
          try{
            //get meta data from solr
            val patternStr = qeurySolrById(key.as[String], logger)
            val patternJson = (Json.parse(patternStr) \ "doc").as[JsValue]

            //not sure if i need to reorganize groum/groumKey in the "pattern", or just include the raw data from solr
            //Yue told me to use the format from Solr, so I set the result from Solr as "pattern"
            val pattern: JsObject = Json.obj("weight" -> weight, "pattern" -> patternJson)
            patternsList += pattern
          }catch {
            case ex: IOException => logger.error(s"Exception occurred while processing Solr response for ${key.as[String]}", ex.getMessage)
          }
      }

      //get cluster_id
      // val cluster_id = (patternsList(0) \ "pattern" \\ "cluster_key_sni")

      // val result: JsObject = Json.obj("cluster_id" -> cluster_id(0), "patterns" -> patternsList)

      val result: JsObject = Json.obj("patterns" -> patternsList)
      result.as[JsValue]

    }

  }

  //call by query provenance route
  def queryProvenance(user: String, repo: String, className: String, method: String, hash: String): JsValue ={
    val result: JsObject = Json.obj("groum" -> "some form of representation", "githubLink" -> "path/path")
    result.as[JsValue]
  }

  def queryGroumBlackBox(user: String, repo: String, method: String, hash: Option[String], logger: LoggingAdapter): JsValue={

    val FixrGraphPatternSearchPY = config.getString("fixr.groums.FixrGraphPatternSearch") + "fixrsearch/search.py"
    val test_env_graph = config.getString("fixr.groums.extractionRepo") + "graphs"
    val test_env_cluster = config.getString("fixr.groums.extractionRepo") + "clusters"
    val FixrGraphIso = config.getString("fixr.groums.FixrGraphIso") + "build/src/fixrgraphiso/fixrgraphiso"
    var result = ""

    val ETPhoneHome = config.getBoolean("fixr.groums.ETPhoneHome")
    var preCmd: Seq[String] = Seq()
    if (ETPhoneHome) {
       val GroumsServerAlias = config.getString("fixr.groums.GroumsServerAlias")
       preCmd = Seq("ssh", GroumsServerAlias) ++ Seq("export", s"PYTHONPATH=${config.getString("fixr.groums.FixrGraph")}python:${config.getString("fixr.groums.FixrGraphPatternSearch")}",";")
    }

    //run Groum search command line
    try {
      hash match {
        case Some(hashcommit) =>
          result = (preCmd ++ Seq("python", FixrGraphPatternSearchPY, "-d", test_env_graph, "-u", user, "-r", repo, "-z", hashcommit, "-m", method, "-c", test_env_cluster, "-i", FixrGraphIso) ) !!
        case None =>
          result = (preCmd ++ Seq("python", FixrGraphPatternSearchPY, "-d", test_env_graph, "-u", user, "-r", repo, "-m", method, "-c", test_env_cluster, "-i", FixrGraphIso)) !!
          // val cmd = Seq("python", FixrGraphPatternSearch, "-d", test_env_graph, "-u", user, "-r", repo, "-m", method, "-c", test_env_cluster, "-i", FixrGraphIso)
          // logger.info("Running command: " + cmd.mkString(" "))
          // result = cmd !!
      }
    } catch {
       case ex: Exception => logger.error(s"Something bad happened: $ex", ex.getMessage)
    }

    logger.info(result)

    println(Json.prettyPrint(Json.parse(result)))
    Json.parse(result)
  }

  def qeurySolrById(id: String, logger: LoggingAdapter): String = {
    try {
      val url = config.getString("fixr.groums.solrURL")
      logger.info(s"Calling Solr at: $url?id=$id")
      val response: HttpResponse[String] = Http(url).param("id", id).timeout(connTimeoutMs = 1000, readTimeoutMs = 5000).asString
      logger.info(s"Results: ${response.body}")
      response.body
    } catch {
      case ex: IOException => ex.asInstanceOf[String]
    }
  }


}
