package edu.colorado.plv.fixr

import java.io.{File, PrintWriter}

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import scala.io.StdIn
import scala.util.parsing.json.JSONObject

/**
  * Created by chihwei on 3/21/17.
  */
object FixrServer {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  val logger = Logging(system, getClass)

  def main(args: Array[String]) {

    val route =
      path("code_search") {
        post {
          entity(as[String]) {
            queryStr => {
              complete{
                logger.info(queryStr)
                try {
                  val solrResponse = new SolrClientSearch().findRecordWithKeyword(queryStr)
                  solrResponse match {
                    case Some(json) =>
                      if (json != Nil) {
                        logger.info(s"Find list of github info")
                        val searchResponse: JsObject = Json.obj("Tile" -> (Json.obj("Type" -> "Commit") ++ Json.obj("SrcsInfo" -> json)))
                        val prettyjson = Json.prettyPrint(searchResponse.as[JsValue])
                        HttpResponse(StatusCodes.OK, entity = s"$prettyjson")
                      } else {
                        HttpResponse(StatusCodes.OK, entity = s"No code pieces Found")
                      }
                    case None => HttpResponse(StatusCodes.InternalServerError,
                      entity = s"Data is not fetched and something went wrong")
                  }
                }catch {
                  case ex: Throwable =>
                    logger.error(ex, ex.getMessage)
                    HttpResponse(StatusCodes.InternalServerError,
                      entity = "Error while querying data")
                }
              }
            }
          }
        }
      } ~ path("refine_query" / "commit") {
        post {
          entity(as[String]) {
            queryStr => {
              complete {
                val queryJson = Json.parse(queryStr)
                logger.info(Json.prettyPrint(queryJson))
                val codePattern = (queryJson \ "Code").as[String]
                val userName = (queryJson \ "UserName").as[String]
                val repoName = (queryJson \ "RepoName").as[String]
                val commit = (queryJson \ "CommitHash").as[String]
                val srcFile = (queryJson \ "SrcFile").as[String]
                try{
                  val solrResponse = new SolrClientSearch().findRecordWithRepoName(userName , repoName, commit, codePattern, srcFile)
                  solrResponse match {
                    case Some(json) =>
                      if(json != Nil) {
                        logger.info(s"Find list of github info")
                        /*val pw = new PrintWriter(new File("test.txt" ))
                        pw.write(Json.prettyPrint(json))
                        pw.close*/
                        val javaCode = codePattern //only one line for now
                        val info = new RefinementParser().getInfo(json, javaCode)

                        val prettyjson = Json.prettyPrint(info.as[JsValue])
                        HttpResponse(StatusCodes.OK, entity = s"$prettyjson")
                      } else {
                        HttpResponse(StatusCodes.OK, entity = s"No code pieces Found")
                      }
                    case None => HttpResponse(StatusCodes.InternalServerError,
                      entity = s"Data is not fetched and something went wrong")
                  }
                } catch {
                  case ex: Throwable =>
                    logger.error(ex, ex.getMessage)
                    HttpResponse(StatusCodes.InternalServerError,
                      entity = "Error while querying data")
                }
              }
            }
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
