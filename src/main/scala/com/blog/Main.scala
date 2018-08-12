package com.blog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.blog.routes.Routes

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn


object Main extends App {
  private val server = WebServer()
  server.start()
  StdIn.readLine() // let it run until user presses return
  server.stop()
}

final case class WebServer() extends Routes {
  implicit val system: ActorSystem = ActorSystem("blog")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private var server: Future[Http.ServerBinding] = _

  def start() = {
    server = Http().bindAndHandle(route, "localhost", 8080)
    log.info(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  }

  override def stop(): Unit = {
    super.stop()
    server
      .flatMap(_.unbind())                  // trigger unbinding from the port
      .onComplete(_ => system.terminate())  // and shutdown when done
  }
}