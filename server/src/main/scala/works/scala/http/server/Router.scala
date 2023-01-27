package works.scala.http.server

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.{ NettyFutureServer, NettyFutureServerBinding }
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.net.InetSocketAddress
import scala.concurrent.Future

trait Router[A]:
  given ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  inline def start(using
      endpoints: List[ServerEndpoint[Any, Future]],
  ): Future[NettyFutureServerBinding[InetSocketAddress]] =

    val swaggerEndpoints: List[ServerEndpoint[Any, Future]] =
      SwaggerInterpreter()
        .fromEndpoints[Future](endpoints.map(_.endpoint), "scala-http", "1.0")

    NettyFutureServer()
      .addEndpoints(endpoints ++ swaggerEndpoints)
      .start()
