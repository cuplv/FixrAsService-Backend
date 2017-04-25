package edu.colorado.plv.fixr

import org.scalatest.{FlatSpec, Matchers}
import edu.colorado.plv.fixr.FixrServer
import play.api.libs.json.{JsValue, Json}

import scalaj.http.{Http, HttpResponse}
import scalaj.http.HttpOptions._

/**
  * Created by chihwei on 3/23/17.
  */
class FixrServerTest extends FlatSpec with Matchers {


  "FixrServer" should "respond when getting the post request at route /compute/method/groums" in{
    val input: JsValue = Json.obj("user" -> "BarcodeEye", "repo" -> "BarcodeEye", "class" -> "Groums", "method" -> "com.google.zxing.client.android.camera.CameraConfigurationManager_setDesiredCameraParameters", "hash" -> "0e59cf40d83d3da67413b0b20410d6c57cca0b9e").as[JsValue]
    val url = "http://localhost:8081/compute/method/groums"
    val output = postJsonUrl(url, input)
    println(output.body)
    assert(output.code === 200)
  }


  def postJsonUrl(url:String, jsonData: JsValue) : HttpResponse[String] = {
    val jsonStr = Json.stringify(jsonData)
    println(s"Calling URL: $url with $jsonStr")
    val result = Http(url).postData(jsonStr)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(connTimeout(10000))
      .option(readTimeout(10000)).asString
    println("Received Results: " + result.code + " " + result.body)
    return result
  }
}
