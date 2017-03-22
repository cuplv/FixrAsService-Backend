package edu.colorado.plv.fixr

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

import spray.json._

/**
  * Created by chihwei on 3/21/17.
  */
object FixrServer {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  def main(args: Array[String]) {

    val l = new SolrClientSearch().findRecordWithKeyword("startTransaction")
    //println(l)
  }


}
