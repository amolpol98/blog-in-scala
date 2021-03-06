package com.blog

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import com.blog.routes.Routes
import org.scalatest.{Matchers, WordSpec}

class RoutesTest extends WordSpec with Matchers with ScalatestRouteTest {
  val testSystem: Routes = new Routes {
    override implicit def system: ActorSystem = ActorSystem("blog")
  }
  val routes: Route = testSystem.routes

  "The server" should {
    "return \"This is Home page\" in response for GET request to the root path" in {
      Get() ~> routes ~> check {
        responseAs[String] should include("This is Home page")
      }
    }
  }

  "return \"About page\" in response for GET request to the /about path" in {
    Get("/about") ~> routes ~> check {
      responseAs[String] should include ("About page")
    }
  }

  "return \"This is Home page\" in response for GET request to /data/home path" in {
    Get("/data/home") ~> routes ~> check {
      responseAs[String] should include ("This is Home page")
    }
  }
  "return \"About page\" in response for GET request to the /data/about path" in {
    Get("/data/about") ~> routes ~> check {
      responseAs[String] should include ("About page")
    }
  }
}
