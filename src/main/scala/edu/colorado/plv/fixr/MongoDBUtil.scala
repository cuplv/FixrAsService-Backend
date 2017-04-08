package edu.colorado.plv.fixr

import com.typesafe.config.ConfigFactory
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import play.api.libs.json._

/**
  * Created by chihwei on 4/7/17.
  */
class MongoDBUtil {
  val config = ConfigFactory.load("application.conf")
  val server = config.getString("fixr.mongodb.server")
  val db = config.getString("fixr.mongodb.database")
  val col = config.getString("fixr.mongodb.collection")

  private def connectDB():MongoCollection = {
    val mongoConn = MongoConnection(server)
    val mongoColl = mongoConn(db)(col)
    mongoColl
  }

  def insertSolrData(json: JsValue): String = {
    val ids = (json \ "_results" \\ "id")
    val srcs = (json \ "_results").as[JsArray].value
    var count = 0
    //println(srcs.length)

    (ids zip srcs).foreach{
      case (id, src) =>
        println(id)
        val obj: JsObject = Json.obj("_id" -> id.as[String], "src" -> src)
        val doc: DBObject = JSON.parse(obj.toString).asInstanceOf[DBObject]
        try {
          val mongoConn = MongoConnection(server)
          val mongoColl = mongoConn(db)(col)
          println("inserting...")
          mongoColl.insert(doc)
          count += 1
        }catch {
          case ex: Throwable => "insertion failed"
        }
    }
    s"$count documents are inserted successfully"
  }
}
