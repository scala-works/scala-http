package works.scala.http.controllers

import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import works.scala.http.server.Controller
import scala.concurrent.Future

case class HeathController() derives Controller:

  private val msg: String = "Alive!"

  val health: ServerEndpoint[Any, Future] = endpoint.get
    .in("health")
    .out(stringBody)
    .serverLogic(_ => Future.successful[Either[Unit, String]](Right(msg)))
