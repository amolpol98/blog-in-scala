package com.blog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.event.Logging
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

import routes.Routes

object Main extends App {
  private val server = WebServer()
  server.start(Routes.route)
  StdIn.readLine() // let it run until user presses return
  server.stop()
}

final case class WebServer() {
  implicit val system: ActorSystem = ActorSystem("blog")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private var server: Future[Http.ServerBinding] = _
  private lazy val log = Logging(system, classOf[WebServer])

  def start(route: Route) = {
    server = Http().bindAndHandle(route, "localhost", 8080)
    log.info(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  }

  def stop() = {
    server
      .flatMap(_.unbind())                  // trigger unbinding from the port
      .onComplete(_ => system.terminate())  // and shutdown when done
  }
}