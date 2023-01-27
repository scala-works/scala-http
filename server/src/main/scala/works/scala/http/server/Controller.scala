package works.scala.http.server

import sttp.tapir.server.ServerEndpoint

import scala.compiletime.*
import scala.concurrent.Future
import scala.deriving.*
import scala.quoted.*

trait Controller[A]:
  extension (a: A) def routes: List[ServerEndpoint[Any, Future]]

object Controller:
  inline def gatherRoutes[A](a: A): List[ServerEndpoint[Any, Future]] = ${
    gatherRoutesImpl[A]('a)
  }

  private def gatherRoutesImpl[A: Type](a: Expr[A])(using
      Quotes,
  ): Expr[List[ServerEndpoint[Any, Future]]] =
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
    val desired       = TypeRepr.of[ServerEndpoint[?, ?]]
    val filtered      = fieldsT.filter {
      case (f, t) if t.typeSymbol.name == desired.typeSymbol.name => true
      case _                                                      => false
    }
    println(s"* Collecting the following fields:")
    filtered.foreach { case (f, t) =>
      println(s"*\t$f: ${ t.typeSymbol.name }")
    }
    val results       = filtered.map { case (f, t) =>
      Select(a.asTerm, f).asExprOf[ServerEndpoint[Any, Future]]
    }
    println(s"**/")
    Expr.ofList(results)

  inline given derived[A <: Product](using m: Mirror.Of[A]): Controller[A] =
    inline m match
      case _: Mirror.SumOf[A]     =>
        error("Auto derivation is not supported for Sum types")
      case p: Mirror.ProductOf[A] =>
        new Controller[A]:
          extension (a: A)
            def routes: List[ServerEndpoint[Any, Future]] = gatherRoutes[A](a)
