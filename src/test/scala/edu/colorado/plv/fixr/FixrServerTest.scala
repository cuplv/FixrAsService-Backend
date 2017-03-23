package edu.colorado.plv.fixr

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{FlatSpec, Matchers}
import edu.colorado.plv.fixr.FixrServer

/**
  * Created by chihwei on 3/23/17.
  */
class FixrServerTest extends FlatSpec with Matchers with ScalatestRouteTest{

  /*
  "FixrServer" should "search data with the keyword in the solr" in{
    val keyword = "startTransaction"
    Post("/code_search", HttpEntity(keyword)) ~>
      route ~>
    check{
      assert(responseAs[String].contains("c_callsites_t:startTransaction OR c_imports_added_t:startTransaction OR c_imports_removed_t:startTransaction") === true)
    }
  }
  */

}
