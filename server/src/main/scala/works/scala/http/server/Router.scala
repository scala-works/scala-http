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
  def collectRoutes(using p: Mirror.ProductOf[A])(
      t: p.MirroredElemTypes,
  ): List[ServerEndpoint[Any, F]]

object TapirRouter:

  inline def summonControllers[T <: Tuple]: List[Controller[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  =>
        summonInline[Controller[t]] :: summonControllers[ts]

  def swaggerEndpoints[F[_]](
      endpoints: List[ServerEndpoint[Any, F]],
  ): List[ServerEndpoint[Any, F]] =
    SwaggerInterpreter()
      .fromEndpoints[F](
        endpoints.map(_.endpoint),
        "scala-http",
        "1.0",
      )

case class MyRouter(a: String, b: Int)

trait Router[A]:
  def controllersOf(a: A): List[Controller[?]]
  extension (a: A) def controllers[F[_]]: List[Controller[?]] = controllersOf(a)

object Router:

  inline given derived[A](using m: Mirror.Of[A]): Router[A] =
    inline m match
      case _: Mirror.SumOf[A]     =>
        error("Auto derivation is not supported for Sum types")
      case p: Mirror.ProductOf[A] =>
        new Router[A]:
          override def controllersOf(a: A): List[Controller[?]] =
            TapirRouter.summonControllers[p.MirroredElemTypes]
