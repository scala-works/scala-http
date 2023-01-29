package works.scala.http

import sttp.tapir.server.ServerEndpoint
import works.scala.http.controllers.{ GreetController, HeathController }
import works.scala.http.server.{ Controller, Router }

import scala.concurrent.Future
import works.scala.http.server.Server
import scala.deriving.Mirror.ProductOf
import sttp.tapir.server.netty.NettyFutureServer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class MyRouter(health: HeathController, greet: GreetController)
    derives Router

object App extends Server[MyRouter]:
  override val dependencies: Future[MyRouter] = Future(
    MyRouter(
      HeathController(),
      GreetController("Scala Works!"),
    ),
  )
  serve
