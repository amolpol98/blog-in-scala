package com.blog.viewmodel

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val homeFormat: RootJsonFormat[HomeViewModel] = jsonFormat2(HomeViewModel)
  implicit val aboutFormat: RootJsonFormat[AboutViewModel] = jsonFormat2(AboutViewModel)
}
