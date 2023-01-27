package works.scala.http.controllers

import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import works.scala.http.server.Controller
import scala.concurrent.Future

case class GreetController(greeting: String) derives Controller:
  val greet: ServerEndpoint[Any, Future] = endpoint.get
    .in("greet")
    .out(stringBody)
    .serverLogic(_ => Future.successful[Either[Unit, String]](Right(greeting)))
