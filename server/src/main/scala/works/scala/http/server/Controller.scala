package works.scala.http.server

import sttp.tapir.server.ServerEndpoint

import scala.compiletime.*
import scala.concurrent.Future
import scala.deriving.*
import scala.quoted.*

trait TapirController[A, F[_]]:
  def routesOf(a: A): List[ServerEndpoint[Any, F]]
  extension (a: A) def routes: List[ServerEndpoint[Any, F]] = routesOf(a)

object TapirController:

  inline def gatherRoutes[A, F[_]](a: A): List[ServerEndpoint[Any, F]] = ${
    gatherRoutesImpl[A, F]('a)
  }

  private def gatherRoutesImpl[A: Type, F[_]: Type](a: Expr[A])(using
      Quotes,
  ): Expr[List[ServerEndpoint[Any, F]]] =
    import quotes.reflect.*
    println(s"/**")
    val controllerRep = TypeRepr.of[A]
    println(s"* Controller: ${ controllerRep.typeSymbol.name }")
    val fields        = TypeTree.of[A].symbol.declaredFields
    val fieldTypes    = fields.map(controllerRep.memberType)
    val fieldsT       = fields.zip(fieldTypes)
    println(s"* Found the following fields:")
    fieldsT.foreach { case (f, t) =>
      println(s"*\t$f: ${ t.typeSymbol.name }")
    }
    // This is only filtering on ServerEndpoint; needs to also filter on ServerEndpoint type arguments
    val desired       = TypeRepr.of[ServerEndpoint[?, F]]
    val filtered      = fieldsT.filter {
      case (f, t) if t.typeSymbol.name == desired.typeSymbol.name => true
      case _                                                      => false
    }
    println(s"* Collecting the following fields:")
    filtered.foreach { case (f, t) =>
      println(s"*\t$f: ${ t.typeSymbol.name }")
    }
    val results       = filtered.map { case (f, t) =>
      Select(a.asTerm, f).asExprOf[ServerEndpoint[Any, F]]
    }
    println(s"**/")
    Expr.ofList(results)

trait Controller[A] extends TapirController[A, Future]

object Controller:
  inline given derived[A](using m: Mirror.Of[A]): Controller[A] =
    inline m match
      case _: Mirror.SumOf[A]     =>
        error("Auto derivation is not supported for Sum types")
      case p: Mirror.ProductOf[A] =>
        new Controller[A]:
          override def routesOf(a: A): List[ServerEndpoint[Any, Future]] =
            TapirController.gatherRoutes[A, Future](a)
