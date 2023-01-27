package works.scala.http.server

object MacroHelpers {

  type IType[T <: Tuple] = Tuple.Fold[T, Any, [x, y] =>> x & y]
  type UType[T <: Tuple] = Tuple.Fold[T, Any, [x, y] =>> x | y]
  
}
