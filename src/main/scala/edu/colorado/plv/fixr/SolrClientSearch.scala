package edu.colorado.plv.fixr

import com.google.gson.Gson
import org.apache.solr.client.solrj.{SolrQuery, SolrServerException}
import org.apache.solr.client.solrj.impl.{HttpSolrClient, XMLResponseParser}
import org.apache.solr.client.solrj.response.QueryResponse
import spray.json._

/**
  * Created by chihwei on 3/21/17.
  */
class SolrClientSearch {

  val url = "http://192.12.243.133:8983/solr/fixr_delta"

  def findRecordWithKeyword(keyword: String): Option[String] = {
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

  private def executeQuery(parameter: SolrQuery): Option[String] = {
    try {
      val solrClient: HttpSolrClient = new HttpSolrClient.Builder(url).build()
      println("set up solrClient")
      solrClient.setParser(new XMLResponseParser());
      val response: QueryResponse = solrClient.query(parameter)
      val gson = new Gson()
      val result = gson.toJson(response)
      val json = result.parseJson.prettyPrint
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
