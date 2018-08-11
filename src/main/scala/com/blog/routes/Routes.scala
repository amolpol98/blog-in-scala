package com.blog.routes

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.blog.engine._

trait Routes {
  implicit def system: ActorSystem
  def log: LoggingAdapter = Logging(system, this.getClass)

  lazy val route: Route = concat (
    index,
    js
  )

  private lazy val nashorn: JavaScriptEngine = new NashornEngine(
    Seq(
      ScriptURL(getClass.getResource("/webapp/js/polyfill/nashorn-polyfill.js")),
      ScriptURL(getClass.getResource("/webapp/js/bundle.js")),
      ScriptText("var frontend = new com.blog.web.Frontend();")
    )
  )

  private val index: Route = {
    pathEndOrSingleSlash {
      get {
        val content = nashorn.invokeMethod[String]("frontend", "renderServer", "Hello World")
        val data = "\"Hello World\""
        val html = views.html.index.render(content, data)
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html.toString()))
      }
    }
  }

  private val js: Route = {
    get {
      pathPrefix("js" / Segment) { file =>
        val js = scala.io.Source.fromURL(getClass.getResource(s"/webapp/js/$file"))("UTF-8").mkString
        complete(HttpEntity(MediaTypes.`application/javascript` withCharset HttpCharsets.`UTF-8`, js))
      }
    }
  }
}
