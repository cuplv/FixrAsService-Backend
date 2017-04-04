package edu.colorado.plv.fixr

import javax.tools.JavaCompiler

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
    val importCode = (json \ "_results" \\ "c_imports_t")


    //get diffs and highlight index
    val diffObj: JsObject= getDiffsAndHighlight(diffCode, javaCode)
    info ++= diffObj

    val featureObj: JsObject = getFeatures(callsiteCode, importCode, javaCode)
    info ++= Json.obj("features" -> featureObj)

    info
  }

  def getDiffsAndHighlight(diffCode: Seq[JsValue], javaCode: String): JsObject = {
    val plus = new ListBuffer[Int]()
    val minus = new ListBuffer[Int]()
    var diffIndex = 0
    var contentIndex = 0;
    val highlight = new ListBuffer[ListBuffer[Int]]()

    diffCode(0).as[String].split("\n").foreach{ line =>
      //println(line)
      line.head match {
        case '+' => plus += diffIndex
        case '-' => minus += diffIndex
        case _ => None
      }

      if(line.contains(javaCode)){
        val highlightIndex = contentIndex + line.indexOf(javaCode)
        highlight += ListBuffer(highlightIndex, highlightIndex + javaCode.length)
      }
      diffIndex += 1
      contentIndex += (line.length + 1)
    }
    val diffs : JsObject = Json.obj("+" -> plus, "-" -> minus)
    Json.obj("diffs" -> diffs, "highlight" -> highlight)
  }

  def getFeatures(callsiteCode: Seq[JsValue], importCode: Seq[JsValue], javaCode: String): JsObject = {
    //get callsite index
    val callsite = new ListBuffer[ListBuffer[Int]]()
    var callsiteIndex = 0
    var regionType = "Default"
    callsiteCode(0).as[String].split("\\) ").foreach{ line =>
      //println(line)
      if(line.contains(javaCode)){
        callsite += ListBuffer(callsiteIndex, callsiteIndex + line.length)
        regionType = "MethodCall"
      }
      callsiteIndex += (line.length + 2)
    }

    //get import index
    val imports = new ListBuffer[ListBuffer[Int]]()
    var importIndex = 0
    importCode(0).as[String].split(" ").foreach{ line =>
      //println(line)
      if(line.contains(javaCode)){
        imports += ListBuffer(importIndex, (importIndex + line.length -1))
        regionType = "Import"
      }
      importIndex += (line.length + 1)
    }

    regionType match {
      case "MethodCall" =>
        val methodCall : JsObject = Json.obj("type" -> regionType, "region" -> callsite)
        methodCall
      case "Import" =>
        val importFeature : JsObject = Json.obj("type" -> regionType, "region" -> imports)
        importFeature
    }
  }
}
