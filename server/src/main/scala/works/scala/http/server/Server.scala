package works.scala.http.server

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*
import scala.concurrent.Future
import sttp.tapir.server.netty.NettyFutureServer
import scala.deriving.Mirror.ProductOf
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import sttp.tapir.server.ServerEndpoint

trait TapirServer[A, F[_]]:
  inline given Mirror.ProductOf[A] = summonInline[Mirror.ProductOf[A]]
  val dependencies: F[A]
  inline def serve(using p: Mirror.ProductOf[A]): F[Unit]

/** Future based implementation
  */
trait Server[A <: Product] extends TapirServer[A, Future] with scala.App:
  given ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  override inline def serve(using p: ProductOf[A]): Future[Unit] =
    val routes = this.dependencies
      .map { d =>
        val router: Router[A] =
          summonInline[Router[A]]

        val controllers: List[TapirController[Any, scala.concurrent.Future]] =
          router
            .controllersOf(d)
            .map(_.asInstanceOf[TapirController[Any, Future]])

        val routes: List[ServerEndpoint[Any, concurrent.Future]] =
          d.productIterator
            .zip(controllers)
            .flatMap { case (c, cc) =>
              cc.routesOf(c)
            }
            .toList

        TapirRouter.swaggerEndpoints(routes) ++ routes
      }
    // Not sure why yet, but you can't seem to flatMap *into* NettyFutureServer().start()
    // so we await the controller generation for now.
    NettyFutureServer()
      .addEndpoints(Await.result(routes, Duration.Inf))
      .start()
      .map(_ => ())
