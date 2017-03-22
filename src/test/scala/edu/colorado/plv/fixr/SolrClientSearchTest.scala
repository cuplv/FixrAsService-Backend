package edu.colorado.plv.fixr

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by chihwei on 3/22/17.
  */
class SolrClientSearchTest extends FlatSpec with Matchers{
  "SolrClient" should "connect to fixr solr server" in{
    val solrClient: HttpSolrClient = new HttpSolrClient.Builder("http://192.12.243.133:8983/solr/fixr_delta").build()
    assert(solrClient !== Nil)
  }

  it should "have the right query command given a keyword" in{
    val fq = setUpQuery("startTransaction")
    assert(fq === "fq=c_callsites_t:startTransaction+OR+c_imports_added_t:startTransaction+OR+c_imports_removed_t:startTransaction")
  }

  def setUpQuery(keyword: String): String = {
    val parameter: SolrQuery = new SolrQuery()
    parameter.set("fq", s"c_callsites_t:$keyword OR c_imports_added_t:$keyword OR c_imports_removed_t:$keyword")
    parameter.toString
  }
}
