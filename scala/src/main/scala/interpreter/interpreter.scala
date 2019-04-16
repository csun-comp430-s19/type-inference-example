package simplescala_experimentation.interpreter

import simplescala_experimentation.polytyped._

object Aliases {
  type Env = Map[Variable, Value]
  type Defs = Map[FunctionName, (Variable, Exp)]
}
import Aliases.{Env, Defs}

class StuckException extends Exception

sealed trait Value
case class StrV(s: String) extends Value
case class BoolV(b: Boolean) extends Value
case class IntV(i: Int) extends Value
case object UnitV extends Value
case class ClosureV(x: Variable, e: Exp, env: Env) extends Value
case class ConstructorV(cn: ConstructorName, v: Value) extends Value
case class TupleV(vs: List[Value]) extends Value

sealed trait Kont
case class BinopLeftK(binop: Binop, e: Exp) extends Kont
case class BinopRightK(v: Value, binop: Binop) extends Kont
case class RestoreK(env: Env) extends Kont
case class AnonFunLeftK(e: Exp) extends Kont
case class AnonFunRightK(x: Variable, e: Exp, env: Env) extends Kont
case class NamedFunK(fn: FunctionName) extends Kont
case class IfK(e1: Exp, e2: Exp) extends Kont
case class BlockK(x: Variable, vals: List[Val], e: Exp) extends Kont
case class TupleK(es: List[Exp], vs: List[Value]) extends Kont
case class ConstructorK(cn: ConstructorName) extends Kont
case class MatchK(cases: List[Case]) extends Kont

sealed trait Term
case class TermExp(e: Exp) extends Term
case class TermValue(v: Value) extends Term

object Interpreter {
  def makeDefs(defsSeq: Seq[Def]): Defs = {
    defsSeq.map(
      { case Def(fn, _, x, _, _, e) => (fn -> (x -> e)) }).toMap
  }

  def apply(defs: Defs): Interpreter = {
    new Interpreter(defs)
  }

  def apply(prog: Program): Interpreter = {
    apply(makeDefs(prog.defs))
  }

  // returns the stuck exception if it got stuck
  def runProgramToOpValue(prog: Program): Either[StuckException, Value] = {
    catchStuckException(runProgramToValue(prog))
  }

  def catchStuckException[A](f: => A): Either[StuckException, A] = {
    try {
      Right(f)
    } catch {
      case e: StuckException => Left(e)
    }
  }

  def runProgramToValue(prog: Program): Value = {
    val interpreter = apply(prog)
    interpreter.initialState(prog.e).runToValue
  }

  def usage() {
    println("Needs the name of an input SimpleScala file")
  }

  def main(args: Array[String]) {
    if (args.length != 1) {
      usage()
    } else {
      val input = SimpleScalaParser.fileContents(args(0))
      SimpleScalaParser.parseProgram(input) match {
        case Left(error) => {
          println(error)
        }
        case Right(program) => {
          //println(program)
          println(runProgramToValue(program))
        }
      }
    }
  } // main
} // Interpreter

class Interpreter(val defs: Defs) {
  def evalOp(v1: Value, binop: Binop, v2: Value): Value = {
    (v1, binop, v2) match {
      case (IntV(i1), BinopPlus, IntV(i2)) => IntV(i1 + i2)
      case (StrV(str1), BinopConcat, StrV(str2)) => StrV(str1 + str2)
      case (IntV(i1), BinopMinus, IntV(i2)) => IntV(i1 - i2)
      case (IntV(i1), BinopTimes, IntV(i2)) => IntV(i1 * i2)
      case (IntV(i1), BinopDiv, IntV(i2)) if i2 != 0 => IntV(i1 / i2)
      case (BoolV(b1), BinopAnd, BoolV(b2)) => BoolV(b1 && b2)
      case (BoolV(b1), BinopOr, BoolV(b2)) => BoolV(b1 || b2)
      case (IntV(i1), BinopLT, IntV(i2)) => BoolV(i1 < i2)
      case (IntV(i1), BinopLTE, IntV(i2)) => BoolV(i1 <= i2)
      case _ => throw new StuckException
    }
  } // evalOp

  def constructorCaseLookup(cn: ConstructorName, cases: List[Case]): (Variable, Exp) = {
    cases match {
      case ConstructorCase(`cn`, x, e) :: _ => (x -> e)
      case _ :: rest => constructorCaseLookup(cn, rest)
      case _ => throw new StuckException
    }
  } // caseLookup

  def tupleCaseLookup(vs: List[Value], env: Env, cases: List[Case]): (Env, Exp) = {
    cases match {
      case TupCase(xs, e) :: _ if vs.size == xs.size => (useTupCase(xs, vs, env) -> e)
      case _ :: rest => tupleCaseLookup(vs, env, rest)
      case _ => throw new StuckException
    }
  } // tupleCaseLookup

  def useTupCase(xs: List[Variable], vs: List[Value], env: Env): Env = {
    (xs, vs) match {
      case (Nil, Nil) => env
      case (x1 :: x2s, v1 :: v2s) => useTupCase(x2s, v2s, env + (x1 -> v1))
      case _ => throw new StuckException
    }
  } // useTupCase

  def initialState(e: Exp): State = {
    State(TermExp(e), Map(), List())
  }
        
  case class State(t: Term, env: Env, ks: List[Kont]) {
    def runToValue(): Value = {
      var state = this
      var value = state.haltedValue
      while (value.isEmpty) {
        state = state.nextState
        value = state.haltedValue
      }
      assert(value.isDefined)
      value.get
    }

    def haltedValue(): Option[Value] = {
      (t, ks) match {
        case (TermValue(v), Nil) => Some(v)
        case _ => None
      }
    }

    def nextState(): State = {
      t match {
        case TermExp(termExp) => {
          termExp match {
            // Rule #1
            case VariableExp(x) => {
              if (env.contains(x)) {
                State(TermValue(env(x)), env, ks)
              } else {
                throw new StuckException
              }
            }
            // Rule #2
            case StringExp(str) => {
              State(TermValue(StrV(str)), env, ks)
            }
            // Rule #3
            case BooleanExp(b) => {
              State(TermValue(BoolV(b)), env, ks)
            }
            // Rule #4
            case IntExp(i) => {
              State(TermValue(IntV(i)), env, ks)
            }
            // Rule #5
            case UnitExp => {
              State(TermValue(UnitV), env, ks)
            }
            // Rule #6
            case BinopExp(e1, op, e2) => {
              State(TermExp(e1), env, BinopLeftK(op, e2) :: ks)
            }
            // Rule #7
            case FunctionExp(x, e) => {
              State(TermValue(ClosureV(x, e, env)), env, ks)
            }
            // Rule #8
            case AnonCallExp(e1, e2) => {
              State(TermExp(e1), env, AnonFunLeftK(e2) :: ks)
            }
            // Rule #9
            case NamedCallExp(fn, e) => {
              if (defs.contains(fn)) {
                State(TermExp(e), env, NamedFunK(fn) :: ks)
              } else {
                throw new StuckException
              }
            }
            // Rule #10
            case IfExp(e1, e2, e3) => {
              State(TermExp(e1), env, IfK(e2, e3) :: ks)
            }
            // Rule #11
            case BlockExp(Val(x, e1) :: vals, e2) => {
              State(TermExp(e1), env, BlockK(x, vals, e2) :: ks)
            }
            // Rule #12
            case TupleExp(e1 :: e2) => {
              State(TermExp(e1), env, TupleK(e2, List()) :: ks)
            }
            // Rule #14
            case ConstructorExp(cn, e) => {
              State(TermExp(e), env, ConstructorK(cn) :: ks)
            }
            // Rule #15
            case MatchExp(e, cases) => {
              State(TermExp(e), env, MatchK(cases) :: ks)
            }
            case _ => {
              throw new StuckException
            }
          } // termExp
        } // case TermExp
        case TermValue(value) => {
          (value, ks) match {
            // Rule #16
            case (v, BinopLeftK(bop, e) :: k2) => {
              State(TermExp(e), env, BinopRightK(v, bop) :: k2)
            }
            // Rule #17
            case (v2, BinopRightK(v1, bop) :: k2) => {
              val v3 = evalOp(v1, bop, v2)
              State(TermValue(v3), env, k2)
            }
            // Rule #18
            case (ClosureV(xP, eP, envP), AnonFunLeftK(e) :: k2) => {
              State(TermExp(e), env, AnonFunRightK(xP, eP, envP) :: k2)
            }
            // Rule #19
            case (v, AnonFunRightK(xP, eP, envP) :: k2) => {
              State(TermExp(eP), envP + (xP -> v), RestoreK(env) :: k2)
            }
            // Rule #20
            case (v, NamedFunK(fn) :: k2) => {
              val (x, e) = defs(fn)
              State(TermExp(e), Map(x -> v), RestoreK(env) :: k2)
            }
            // Rule #21
            case (BoolV(true), IfK(e1, e2) :: k2) => {
              State(TermExp(e1), env, k2)
            }
            // Rule #22
            case (BoolV(false), IfK(e1, e2) :: k2) => {
              State(TermExp(e2), env, k2)
            }
            // Rule #23
            case (v, BlockK(x1, Val(x2, e1) :: vals, e2) :: k2) => {
              State(TermExp(e1), env + (x1 -> v), BlockK(x2, vals, e2) :: RestoreK(env) :: k2)
            }
            // Rule #24
            case (v, BlockK(x, Nil, e) :: k2) => {
              State(TermExp(e), env + (x -> v), RestoreK(env) :: k2)
            }
            // Rule #25
            case (v1, TupleK(e1 :: e2, v2) :: k2) => {
              State(TermExp(e1), env, TupleK(e2, v1 :: v2) :: k2)
            }
            // Rule #26
            case (v1, TupleK(Nil, v2) :: k2) => {
              State(TermValue(TupleV((v1 :: v2).reverse)), env, k2)
            }
            // Rule #28
            case (v, ConstructorK(cn) :: k2) => {
              State(TermValue(ConstructorV(cn, v)), env, k2)
            }
            // Rule #29
            case (ConstructorV(cn, v), MatchK(cases) :: k2) => {
              val (x, e) = constructorCaseLookup(cn, cases)
              State(TermExp(e), env + (x -> v), RestoreK(env) :: k2)
            }
            // Rule #30
            case (TupleV(v1s), MatchK(cases) :: k2) => {
              val (envP, e) = tupleCaseLookup(v1s, env, cases)
              State(TermExp(e), envP, RestoreK(env) :: k2)
            }
            // Rule #31
            case (v, RestoreK(envP) :: k2) => {
              State(TermValue(v), envP, k2)
            }
            // Rule #32
            case (v, Nil) => {
              State(TermValue(v), env, ks)
            }
            case _ => {
              throw new StuckException
            }
          } // (value, ks) match
        } // case ValueExp
      } // t match
    } // nextState
  } // State
} // Interpreter

