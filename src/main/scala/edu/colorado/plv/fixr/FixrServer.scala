package edu.colorado.plv.fixr

import java.io.{File, PrintWriter}

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers
import akka.stream.ActorMaterializer
import play.api.libs.json._

import scala.io.StdIn

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
                val queryJson = Json.parse(queryStr)
                logger.info(queryStr)
                val codePattern = (queryJson \ "Code").as[String]
                try {
                  val solrResponse = new SolrClientSearch().findRecordWithKeyword(codePattern)
                  solrResponse match {
                    case Some(json) =>
                      if (json != Nil) {
                        logger.info(s"Inserting to MongoDB")
                        val dbResult = new MongoDBUtil().insertSolrData(json)
                        logger.info(dbResult)
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
                  //val solrResponse = new SolrClientSearch().findRecordWithRepoName(userName , repoName, commit, codePattern, srcFile)
                  val mongoQuery = new MongoDBUtil().findRecordWithId(userName, repoName, commit, srcFile)
                  HttpResponse(StatusCodes.OK, entity = mongoQuery)
                  if(mongoQuery == "invalid ID"){
                    HttpResponse(StatusCodes.InternalServerError,
                      entity = s"Data is not fetched and something went wrong")
                  }
                  else{
                    val result = Json.parse(mongoQuery)
                    logger.info(s"Find list of github info")
                    /*val pw = new PrintWriter(new File("test.txt" ))
                    pw.write(Json.prettyPrint(json))
                    pw.close*/
                    val javaCode = codePattern
                    val infoList = new RefinementParser().getInfo(result, javaCode)
                    val infoJson: JsObject = Json.obj("Result" -> infoList)
                    val prettyjson = Json.prettyPrint(infoJson.as[JsValue])
                    HttpResponse(StatusCodes.OK, entity = s"$prettyjson")
                  }

                  /*solrResponse match {
                    case Some(json) =>
                      if(json != Nil) {
                        logger.info(s"Find list of github info")
                        /*val pw = new PrintWriter(new File("test.txt" ))
                        pw.write(Json.prettyPrint(json))
                        pw.close*/
                        val javaCode = codePattern
                        val info = new RefinementParser().getInfo(json, javaCode)

                        val prettyjson = Json.prettyPrint(info.as[JsValue])
                        HttpResponse(StatusCodes.OK, entity = s"$prettyjson")
                      } else {
                        HttpResponse(StatusCodes.OK, entity = s"No code pieces Found")
                      }
                    case None => HttpResponse(StatusCodes.InternalServerError,
                      entity = s"Data is not fetched and something went wrong")
                  }*/

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
      } ~ path("compute" / "method" / "groums" ){
        post{
          entity(as[String]){
            queryStr => {
              complete{
                logger.info("IN compute/method/groums")
                val queryJson = Json.parse(queryStr)
                logger.info(Json.prettyPrint(queryJson))

                //parsing the json format
                val user = (queryJson \ "user").as[String]
                val repo = (queryJson \ "repo").as[String]
                val className = (queryJson \ "class").as[String]
                val method = (queryJson \ "method").as[String]
                val hash = (queryJson \ "hash").asOpt[String]

                //search groums
                val output = new GroumsService().searchGroums(user, repo, className, method, hash)
                val prettyjson = Json.prettyPrint(output)
                HttpResponse(StatusCodes.OK, entity = s"$prettyjson")
              }
            }
          }
        }
      } ~ path("query" / "provenance" / "groums"){
        get{
          parameters('user.as[String], 'repo.as[String], 'class.as[String], 'method.as[String], 'hash.as[String]){
            (user, repo, className, method, hash) =>
            complete{
              logger.info("IN query/provenance/groums")
              logger.info(s"User: $user, Repo: $repo, Class: $className, Method: $method, Hash: $hash")
              val output = new GroumsService().queryProvenance(user, repo, className, method, hash)
              val prettyjson = Json.prettyPrint(output)
              HttpResponse(StatusCodes.OK, entity = s"$prettyjson")
            }
          }

        }
      }


    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8081)

    println(s"Server online at http://0.0.0.0:8081/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
