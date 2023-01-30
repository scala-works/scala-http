package works.scala.http.server

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

object MacroHelpers:

  /** An Intersection type, but with the default of Any if empty. Useful for
    * turning CaseClass(a: A, b: B, ...) into a type A & B & ... via a Mirror.
    */
  type IType[T <: Tuple] = Tuple.Fold[T, Any, [x, y] =>> x & y]

  /** A Union type, but with the default of Any if empty. Useful for turning
    * CaseClass(a: A, b: B, ...) into a type A | B | ... via a Mirror.
    */
  type UType[T <: Tuple] = Tuple.Fold[T, Any, [x, y] =>> x | y]

  /** Summon type class instances from case class constructor arguments via
    * Mirrors. For example, for CaseClass(a: A, b: B), we can use this to summon
    * a List[Controller[ A | B ]]
    *
    * @return
    */
  inline def summonListOf[T <: Tuple, A[_]]: List[A[UType[T]]] =
    _summonListOf[T, UType[T], A]

  /** A helper method for summonListOf, since the main operations is recursive
    * for T, and we would lose a type in the Tuple each time, we keep a version
    * of it constant - U, used as UType[T].
    * @return
    */
  private inline def _summonListOf[T <: Tuple, U, A[U]]: List[A[U]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  =>
        summonInline[A[t]].asInstanceOf[A[U]] :: _summonListOf[ts, U, A]
