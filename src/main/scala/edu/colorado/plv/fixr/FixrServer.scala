package edu.colorado.plv.fixr

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import spray.json._

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
                logger.info(queryStr)
                try {
                  val solrResponse = new SolrClientSearch().findRecordWithKeyword(queryStr)
                  solrResponse
                }catch {
                  case ex: Throwable =>
                    logger.error(ex, ex.getMessage)
                    HttpResponse(StatusCodes.InternalServerError,
                      entity = "Error while persisting data")
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
