package edu.colorado.plv.fixr

import play.api.libs.json.{JsObject, JsValue, Json}

import scala.collection.mutable.ListBuffer

/**
  * Created by chihwei on 4/2/17.
  */
class RefinementParser {

  def getInfo(json: JsValue, javaCode:String): JsObject = {
    var info: JsObject = Json.obj("Code" -> javaCode)
    val diffCode = (json \ "_results" \\ "c_patch_t")
    val callsiteCode = (json \ "_results" \\ "c_callsites_t")
    val plus = new ListBuffer[Int]()
    val minus = new ListBuffer[Int]()
    var lineIndex = 0
    diffCode(0).as[String].split("\n").foreach{ line =>
      //println(line)
      if(line.head == '+'){
        plus += lineIndex
      } else if (line.head == '-'){
        minus += lineIndex
      }
      lineIndex += 1
    }

    val callsite = new ListBuffer[Int]
    var callsiteIndex = 0
    callsiteCode(0).as[String].split(" ").foreach{ line =>
      println(line)
      if(line.contains(javaCode)){
        callsite += callsiteIndex
      }
      callsiteIndex += 1
    }
    val diffs : JsObject = Json.obj("+" -> plus, "-" -> minus)
    val methodCall : JsObject = Json.obj("type" -> "MethodCall", "region" -> callsite)
    info ++= Json.obj("diffs" -> diffs, "features" -> methodCall)
    info
  }
}
