package edu.colorado.plv.fixr
import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import org.apache.solr.client.solrj.{SolrQuery, SolrServerException}
import org.apache.solr.client.solrj.impl.{HttpSolrClient, XMLResponseParser}
import org.apache.solr.client.solrj.response.QueryResponse
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable.ListBuffer


/**
  * Created by chihwei on 3/21/17.
  */
class SolrClientSearch {

  val config = ConfigFactory.load("application.conf")
  val url = config.getString("fixr.solr.url")
  val collection_name = config.getString("fixr.solr.collection")
  val url_final = url + collection_name
  val logger = LoggerFactory.getLogger("SolrClient")

  def findRecordWithKeyword(keyword: String): Option[JsValue] = {
    val fq = setFeildQuery(parseCode(keyword))
    try {
      val parameter: SolrQuery = new SolrQuery()
      parameter.set("qt", "/select")
      parameter.set("indent", "on")
      parameter.set("q", "*:*")
      parameter.set("fq", fq)
      parameter.set("wt", "json")
      executeQuery(parameter)
    } catch {
      case solrServerException: SolrServerException =>
        println("Solr Server Exception : " + solrServerException.getMessage)
        None
    }
  }

  def findRecordWithRepoName(username: String, reponame: String, commitHash: String, keyword: String, srcFile: String): Option[JsValue] = {
    try {
      val repo = username + "/" + reponame
      val parameter: SolrQuery = new SolrQuery()
      parameter.set("qt", "/select")
      parameter.set("indent", "on")
      parameter.set("q", s"repo_sni=$repo AND c_hash_sni=$commitHash AND name_sni=$srcFile")
      parameter.set("fq", s"c_patch_t:$keyword")
      parameter.set("wt", "json")
      executeQuery(parameter)
    } catch {
      case solrServerException: SolrServerException =>
        println("Solr Server Exception : " + solrServerException.getMessage)
        None
    }
  }

  private def executeQuery(parameter: SolrQuery): Option[JsValue] = {
    try {
      val solrClient: HttpSolrClient = new HttpSolrClient.Builder(url_final).build()
      println("set up solrClient")
      solrClient.setParser(new XMLResponseParser())
      val response: QueryResponse = solrClient.query(parameter)
      val gson = new Gson()
      val result = gson.toJson(response)
      val json = Json.parse(result)
      solrClient.close()
      Some(json)
    } catch {
      case solrServerException: SolrServerException =>
        println("Solr Server Exception : " + solrServerException.getMessage)
        None
    }
  }

  def parseCode(parsableCode: String): ListBuffer[String] ={
    val codeBuffer = new ListBuffer[String]()

    parsableCode.split("\n").foreach{ line =>

      var newLine: String = line

      if(newLine.contains(".")){
        newLine = newLine.drop(newLine.indexOf(".")+1)
      }

      if(newLine.contains("(")){
        newLine = newLine.dropRight(newLine.length - newLine.indexOf("("))
      }

      codeBuffer += newLine
    }
    codeBuffer
  }

  def setFeildQuery(codeBuffer: ListBuffer[String]): String = {
    var c_callsites_t:String = ""
    var c_callsites_added_t: String = ""
    var c_callsites_removed_t: String = ""

    codeBuffer.foreach{ code =>
      //c_callsites_t += "c_callsites_t:" + code + " AND "
      c_callsites_added_t += "c_callsites_added_t:" + code + " AND "
      c_callsites_removed_t += "c_callsites_removed_t:" + code + " AND "
    }

    //"(" + c_callsites_t.dropRight(5) + ") OR (" + c_callsites_added_t.dropRight(5) + ") OR (" + c_callsites_removed_t.dropRight(5) + ")"
    "(" + c_callsites_added_t.dropRight(5) + ") OR (" + c_callsites_removed_t.dropRight(5) + ")"
  }

}
