package edu.colorado.plv.fixr

import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import org.apache.solr.client.solrj.{SolrQuery, SolrServerException}
import org.apache.solr.client.solrj.impl.{HttpSolrClient, XMLResponseParser}
import org.apache.solr.client.solrj.response.QueryResponse
import play.api.libs.json.{JsValue, Json}


/**
  * Created by chihwei on 3/21/17.
  */
class SolrClientSearch {

  val config = ConfigFactory.load("application.conf")
  val url = config.getString("fixr.solr.url")
  val collection_name = config.getString("fixr.solr.collection")
  val url_final = url + collection_name

  def findRecordWithKeyword(keyword: String): Option[JsValue] = {
    try {
      val parameter: SolrQuery = new SolrQuery()
      parameter.set("qt", "/select")
      parameter.set("indent", "on")
      parameter.set("q", "*:*")
      parameter.set("fq", s"c_callsites_t:$keyword OR c_imports_added_t:$keyword OR c_imports_removed_t:$keyword")
      parameter.set("wt", "json")
      executeQuery(parameter)
    } catch {
      case solrServerException: SolrServerException =>
        println("Solr Server Exception : " + solrServerException.getMessage)
        None
    }
  }

  def findRecordWithRepoName(username: String, reponame: String, commitHash: String, keyword: String): Option[JsValue] = {
    try {
      val repo = username + "/" + reponame
      val parameter: SolrQuery = new SolrQuery()
      parameter.set("qt", "/select")
      parameter.set("indent", "on")
      parameter.set("q", s"repo_sni=$repo AND c_hash_sni=$commitHash")
      parameter.set("fq", s"c_callsites_t:$keyword OR c_imports_added_t:$keyword OR c_imports_removed_t:$keyword")
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
      solrClient.setParser(new XMLResponseParser());
      val response: QueryResponse = solrClient.query(parameter)
      val gson = new Gson()
      val result = gson.toJson(response)
      val json = Json.parse(result)
      //println(json)
      solrClient.close()
      Some(json)
    } catch {
      case solrServerException: SolrServerException =>
        println("Solr Server Exception : " + solrServerException.getMessage)
        None
    }
  }

}
