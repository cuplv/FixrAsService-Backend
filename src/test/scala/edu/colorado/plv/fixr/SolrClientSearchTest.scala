package edu.colorado.plv.fixr

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

/**
  * Created by chihwei on 3/22/17.
  */
class SolrClientSearchTest extends FlatSpec with Matchers{
  "SolrClient" should "connect to fixr solr server" in{
    val solrClient: HttpSolrClient = new HttpSolrClient.Builder("http://192.12.243.133:8983/solr/fixr_delta").build()
    assert(solrClient !== Nil)
  }

  it should "delete the characters appearing before \".\"" in {
    val code = "db.startTransaction"
    val result = new SolrClientSearch().parseCode(code)
    assert(result === ListBuffer("startTransaction"))
  }

  it should "delete the characters appearing after the \"(\"" in {
    val code = "startTransaction(int a)"
    val result = new SolrClientSearch().parseCode(code)
    assert(result === ListBuffer("startTransaction"))
  }

  it should "delete all the characters appearing before \".\" and after \"(\"" in {
    val code = "db.startTransaction(int a, int b)"
    val result = new SolrClientSearch().parseCode(code)
    assert(result === ListBuffer("startTransaction"))
  }

  it should "parse multiple codes and delete unused characters" in {
    val code = "db.startTransaction(int a, int b)\ndb.startTransaction()\nstartTransaction(int a)"
    val result = new SolrClientSearch().parseCode(code)
    assert(result === ListBuffer("startTransaction", "startTransaction", "startTransaction"))
  }

  it should "generate correct field query" in {
    val codeBuffer = ListBuffer("startTransaction", "peekMediaPlayer")
    var result = new SolrClientSearch().setFeildQuery(codeBuffer)
    assert(result === "(c_callsites_t:startTransaction AND c_callsites_t:peekMediaPlayer) OR (imports_added_t:startTransaction AND imports_added_t:peekMediaPlayer) OR (c_imports_removed_t:startTransaction AND c_imports_removed_t:peekMediaPlayer)")
  }
}
