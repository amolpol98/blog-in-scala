package com.blog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import com.blog.routes.Routes

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.util.{Failure, Success}
import scala.concurrent.duration._


object Main extends App {
  private val server = WebServer()
  server.start()
}

final case class WebServer() extends Routes {
  implicit val system: ActorSystem = ActorSystem("blog")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val bindingAddress = "0.0.0.0"
  val bindingPort = 8080
  val shutdownTimeout = 60

  def start() = {
    log.info(s"Starting server on $bindingAddress:$bindingPort")
    Http().bindAndHandle(routes, bindingAddress, bindingPort)
      .onComplete{
        case Success(binding) =>
          val address = binding.localAddress
          registerShutdownHook(binding)
          log.info(s"Server is listening on ${address.getHostString}:${address.getPort}")
        case Failure(ex) =>
          log.error("Server could not be started", ex)
          stop()
      }
  }

  override def stop(): Unit = {
    log.info("Server is being shut down")
    super.stop()
    system.terminate()
    Await.result(system.whenTerminated, shutdownTimeout seconds)
  }

  private def registerShutdownHook(binding: ServerBinding) = {
    sys.addShutdownHook{
      binding.unbind().onComplete( _ => stop())
    }
  }
}