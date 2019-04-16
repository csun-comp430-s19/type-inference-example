package simplescala_experimentation.polytyped

import java.io.File

import org.scalatest.FunSuite

import simplescala_experimentation.interpreter.Interpreter

object TestSuite {
  def prettyTypes(ts: Seq[Type]): String = {
    ts.map(prettyType).mkString("(", ", ", ")")
  }

  def prettyType(t: Type): String = {
    t match {
      case StringType => "String"
      case BooleanType => "Boolean"
      case IntegerType => "Integer"
      case UnitType => "Unit"
      case FunctionType(t1, t2) => prettyType(t1) + " => " + prettyType(t2)
      case TupleType(ts) => prettyTypes(ts)
      case UserType(UserDefinedTypeName(name), ts) => name + prettyTypes(ts)
      case TypeVariableType(TypeVariable(name)) => name
      case TypePlaceholder(n) => "._" + n
    }
  } // prettyType

  // isomorphic means that they vary only by the actual ids
  // used for the placeholders.  This means that not only
  // should the two types unify, placeholders should only ever
  // be unified with other placeholders.
  def typesIsomorphic(t1: Type, t2: Type): Boolean = {
    ConstraintStore(Map()).unify(t1, t2).map(cs =>
      cs.map.keys.forall(key =>
        cs.lookup(key) match {
          case TypePlaceholder(_) => true
          case _ => false
        })).getOrElse(false)
  }
}

class TestSuite(val defsFile: Option[File], private val testContainer: TestContainer) extends FunSuite {
  // ---BEGIN CONSTRUCTOR---
  type FunctionNames = Set[String]
  defsFile.map(file =>
    test("defs file exists and is a file") {
      assert(file.isFile)
    })
  private val parsedDefs: Option[(OnlyDefs, FunctionNames)] =
    defsFile.map(file => {
      val defs =
        unwrap("Bad defs file",
               SimpleScalaParser.parseOnlyDefs(
                 SimpleScalaParser.fileContents(file)))
      (defs -> defs.defs.map(_.fn.name).toSet)
    })
  testContainer.foreachFile(runTest)
  // ---END CONSTRUCTOR---

  private def unwrap[A](msgBegin: => String, either: Either[String, A]): A = {
    either match {
      case Left(msg) => {
        fail(msgBegin + ": " + msg)
      }
      case Right(a) => a
    }
  }

  def failWithStackTrace(msg: String, t: Throwable) {
    import java.io.{StringWriter, PrintWriter}
    val sw = new StringWriter
    val pw = new PrintWriter(sw)
    t.printStackTrace(pw)
    fail(msg + "\n" + t.getMessage + "\n" + sw.toString)
  }

  private def runTest(fileName: String, fileContents: String) {
    test(fileName) {
      val FullTest(testName, prog, expected) = parseTest(fileName, fileContents)
      (Typechecker.eitherType(prog), expected) match {
        case (Left(e), ExpectIllTyped) => ()
        case (Left(e), ExpectWellTypedStuck(_) | ExpectWellTypedValue(_, _) | ExpectWellTypedNoValue(_)) => {
          failWithStackTrace(
            "Typechecker returned ill-typed, but wasn't expected to return ill-typed",
            e)
        }
        case (Right(gotType), ExpectIllTyped) => {
          fail("Typechecker returned type " + TestSuite.prettyType(gotType) +
               ", but was expected to be ill-typed")
        }
        case (Right(gotType), ExpectWellTypedStuck(expectType)) => {
          runOnInterpreterWithTypeTest(prog, gotType, expectType, None)
        }
        case (Right(gotType), ExpectWellTypedValue(expectValue, expectType)) => {
          runOnInterpreterWithTypeTest(prog, gotType, expectType, Some(expectValue))
        }
        case (Right(gotType), ExpectWellTypedNoValue(expectType)) => {
          ensureTypesSame(gotType, expectType)
        }
      }
    }
  }

  private def parseTest(fileName: String, fileContents: String): FullTest = {
    val isSExp = TestContainer.isSimpleScalaSExp(fileName)
    val maybeTest =
      parsedDefs match {
        case Some((OnlyDefs(tdefs, defs), functionNames)) => {
          assert(!isSExp)
          SimpleScalaParser.parseExpTestExpect(functionNames, fileContents, defs).right.map(
            { case (exp, te) => FullTest(fileName, Program(tdefs, defs, exp), te) })
        }
        case None => {
          if (isSExp) {
            SimpleScalaSExpParser.parseFullTest(fileName, fileContents)
          } else {
            SimpleScalaParser.parseFullTest(fileName, fileContents)
          }
        }
      }
    unwrap("Bad test file: '" + fileName + "'",
           maybeTest)
  }

  private def ensureTypesSame(gotType: Type, expectType: Type) {
    if (!TestSuite.typesIsomorphic(gotType, expectType)) {
      fail("Typechecker returned type " + TestSuite.prettyType(gotType) +
           ",  but was expected to get type " + TestSuite.prettyType(expectType))
    }
  }

  private def runOnInterpreterWithTypeTest(prog: Program, gotType: Type, expectType: Type, opV: Option[TestValue]) {
    ensureTypesSame(gotType, expectType)
    runOnInterpreter(prog, opV)
  }

  private def runOnInterpreter(prog: Program, expect: Option[TestValue]) {
    (Interpreter.runProgramToOpValue(prog), expect) match {
      case (Left(_), None) => ()
      case (Right(v), Some(tv)) => {
        tv.compareString(v) match {
          case Some(error) => fail(error)
          case None => ()
        }
      }
      case (Left(e), Some(_)) => {
        fail("Got stuck, but wasn't expected to get stuck", e)
      }
      case (Right(v), None) => {
        fail("Was expected to get stuck, but instead got value: " + TestValue.prettyString(v))
      }
    }
  }
}
