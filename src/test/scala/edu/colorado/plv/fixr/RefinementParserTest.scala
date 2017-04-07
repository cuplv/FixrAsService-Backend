package edu.colorado.plv.fixr

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.collection.mutable.ListBuffer

/**
  * Created by chihwei on 4/5/17.
  */
class RefinementParserTest extends FlatSpec with Matchers{

  "RefinementParser" should "return the indices of diffs and the keyword correctly" in{
    val testDiffs: String = "@Override\n+\tpublic void onDestroy() {\n-\t\tonStopped();\n \t\tgetMediaPlayer().release();\n \t}"
    val testjavaCode: String = "onDestroy"
    val testOutput: JsObject = new RefinementParser().getDiffsAndHighlight(testDiffs, testjavaCode)
    //print(Json.prettyPrint(testOutput.as[JsValue]))
    val diff = (testOutput \ "diffs" )
    val highlight = (testOutput \ "highlight")
    assert( (diff \ "+").as[ListBuffer[Int]] === ListBuffer(1))
    assert( (diff \ "-").as[ListBuffer[Int]] === ListBuffer(2))
    assert( highlight.as[ListBuffer[ListBuffer[Int]]] === ListBuffer(ListBuffer(24,32)))
  }

}
