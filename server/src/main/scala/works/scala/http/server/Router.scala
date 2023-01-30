package works.scala.http.server

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.{ NettyFutureServer, NettyFutureServerBinding }
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.net.InetSocketAddress
import scala.concurrent.Future
import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

trait TapirRouter[A, F[_]]:
  def controllersOf(a: A): List[TapirController[?, F]]
  extension (a: A)
    def controllers: List[TapirController[?, F]] = controllersOf(a)

object TapirRouter:

  def swaggerEndpoints[F[_]](
      endpoints: List[ServerEndpoint[Any, F]],
  ): List[ServerEndpoint[Any, F]] =
    SwaggerInterpreter()
      .fromEndpoints[F](
        endpoints.map(_.endpoint),
        "scala-http",
        "1.0",
      )

trait Router[A] extends TapirRouter[A, Future]

object Router:

  inline given derived[A](using m: Mirror.Of[A]): Router[A] =
    inline m match
      case _: Mirror.SumOf[A]     =>
        error("Auto derivation is not supported for Sum types")
      case p: Mirror.ProductOf[A] =>
        new Router[A]:
          override def controllersOf(
              a: A,
          ): List[TapirController[?, scala.concurrent.Future]] =
            MacroHelpers.summonListOf[p.MirroredElemTypes, Controller]
