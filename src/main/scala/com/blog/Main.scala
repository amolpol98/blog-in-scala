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
  private val config: ServerConfig = ServerConfig.config

  def start() = {
    log.info(s"Starting server on ${config.bindingAddress}:${config.bindingPort}")
    Http().bindAndHandle(routes, config.bindingAddress, config.bindingPort)
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
    Await.result(system.whenTerminated, config.shutdownTimeout)
  }

  private def registerShutdownHook(binding: ServerBinding) = {
    sys.addShutdownHook{
      binding.unbind().onComplete( _ => stop())
    }
  }
}