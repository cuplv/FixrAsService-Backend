package edu.colorado.plv.fixr

import com.google.gson.Gson
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
        //println(src)
        val obj: JsObject = Json.obj("_id" -> id.as[String], "src" -> src)
        val doc: DBObject = JSON.parse(obj.toString).asInstanceOf[DBObject]
        try {
          val collection = connectDB()
          println("inserting... #"+count)
          collection.insert(doc)
          count += 1
        }catch {
          case e: MongoException => "MongoDB insertion error: " + e
        }
    }
    s"$count documents are inserted successfully"
  }

  def findRecordWithId(username: String, reponame: String, commitHash: String, srcFile: String): String = {
    //id = repo_sni#0;name_sni#0;p_hash_sni#0;c_hash_sni
    val tag = "#0;"
    val id = username + "/" + reponame + tag + srcFile + tag + commitHash
    try {
      val collection = connectDB()
      val query = MongoDBObject("_id" -> id)
      val result = collection.findOne(query)
      println(result)
      val commitTile = result.flatMap(_.getAs[DBObject]("src"))
      println(commitTile)
      commitTile match{
        case Some(tileObj) =>
          val gson = new Gson()
          gson.toJson(tileObj)
        case None => "invalid ID"
      }
    } catch {
      case e: MongoException => "MongoDB query error: " + e
    }
  }
}
