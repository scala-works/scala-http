package works.scala.http

import sttp.tapir.server.ServerEndpoint
import works.scala.http.controllers.{ GreetController, HeathController }
import works.scala.http.server.{ Controller, Router }

import scala.concurrent.Future

object App extends App with Router[Any]:

  inline given List[ServerEndpoint[Any, Future]] = List(
    HeathController().routes,
    GreetController("Scala Works!").routes,
  ).flatten

  start
