package simplescala_experimentation.polytyped

object Printer {
  def indentString(indent: Int): String = {
    " " * indent
  }

  def inSep(begin: String, what: String, end: String): String = {
    begin + what + end
  }

  def inParen(what: String): String = {
    inSep("(", what, ")")
  }

  def prettyBop(op: Binop): String = {
    op match {
      case BinopPlus => "+"
      case BinopMinus => "-"
      case BinopTimes => "*"
      case BinopDiv => "/"
      case BinopAnd => "&&"
      case BinopOr => "||"
      case BinopLT => "<"
      case BinopLTE => "<="
      case BinopConcat => "++"
    }
  }

  def prettyExp(indent: Int, exp: Exp): String = {
    exp match {
      case VariableExp(Variable(name)) => name
      case StringExp(str) => "\"" + str + "\""
      case BooleanExp(b) => b.toString
      case IntExp(i) => i.toString
      case UnitExp => "unit"
      case BinopExp(e1, op, e2) => {
        (inParen(prettyExp(indent, e1)) +
         inSep(" ", prettyBop(op), " ") +
         inParen(prettyExp(indent, e2)))
      }
      case FunctionExp(x, e) => {
        x.name + " => " + prettyExp(indent, e)
      }
      case AnonCallExp(e1, e2) => {
        (inParen(prettyExp(indent, e1)) +
         inParen(prettyExp(indent, e2)))
      }
      case NamedCallExp(FunctionName(name), e) => {
        name + inParen(prettyExp(indent, e))
      }
      case IfExp(e1, e2, e3) => {
        ("if " + inParen(prettyExp(indent, e1)) +
         "\n" + indentString(indent) + prettyExp(indent + 1, e2) +
         "\n" + indentString(indent) + " else \n" + 
         indentString(indent + 1) + prettyExp(indent + 1, e3))
      }
      case BlockExp(vals, e) => {
        ("{ " +
         vals.map( { case Val(Variable(name), e) => "val " + name + "= " + prettyExp(indent, e) } )
           .mkString("\n" + indentString(indent)) +
         "\n" + indentString(indent) + prettyExp(indent, e) +
         "\n" + indentString(indent) + "}")
      }
      case TupleExp(es) => {
        inParen(es.map(e => prettyExp(indent, e)).mkString(", "))
      }
      case ConstructorExp(ConstructorName(name), e) => {
        name + inParen(prettyExp(indent, e))
      }
      case MatchExp(e, cases) => {
        (inParen(prettyExp(indent, e)) + " match {\n" + indentString(indent) +
         cases.map(c => prettyCase(indent, c)).mkString("\n" + indentString(indent)) +
         "\n" + indentString(indent) + "}")
      }
    }
  } // prettyExp

  def prettyCase(indent: Int, c: Case): String = {
    c match {
      case ConstructorCase(ConstructorName(cName), Variable(xName), e) => {
        "case " + cName + inParen(xName) + " => " + prettyExp(indent, e)
      }
      case TupCase(xs, e) => {
        "case " + inParen(xs.map(_.name).mkString(", ")) + " => " + prettyExp(indent, e)
      }
    }
  }

  def prettyTypeVars(tvs: Seq[TypeVariable]): String = {
    inSep("[", tvs.map(_.name).mkString(", "), "]")
  }

  def prettyFDef(d: Def): String = {
    ("def " + d.fn.name +
     prettyTypeVars(d.ts) +
     inParen(d.x.name + ": " + prettyType(d.tau1)) +
     ": " + prettyType(d.tau2) +
     " =\n" + indentString(1) + prettyExp(1, d.e))
  }

  def prettyCDef(c: ConstructorDefinition): String = {
    c.cn.name + inParen(prettyType(c.tau))
  }

  def prettyTDef(d: UserDefinedTypeDef): String = {
    ("algebraic " + d.un.name +
     prettyTypeVars(d.ts) +
     " = " +
     d.cdefs.map(prettyCDef).mkString(" | "))
  }

  def prettyProgram(p: Program): String = {
    (p.tdefs.map(prettyTDef).mkString("\n") +
     p.defs.map(prettyFDef).mkString("\n") +
     "\n" + prettyExp(0, p.e))
  }

  def prettyType(t: Type): String = {
    t match {
      case StringType => "String"
      case BooleanType => "Boolean"
      case IntegerType => "Int"
      case UnitType => "Unit"
      case FunctionType(t1, t2) => inParen(prettyType(t1) + " => " + prettyType(t2))
      case TupleType(ts) => inParen(ts.map(prettyType).mkString(", "))
      case UserType(UserDefinedTypeName(name), ts) => name + inSep("[", ts.map(prettyType).mkString(", "), "]")
      case TypeVariableType(TypeVariable(name)) => name
      case TypePlaceholder(n) => "._" + n.toString
    }
  }

  def prettyTestExpect(te: TestExpect): String = {
    te match {
      case ExpectIllTyped => "ILLTYPED"
      case ExpectWellTypedStuck(t) => "STUCK\n;;;\n" + prettyType(t)
      case ExpectWellTypedValue(tv, t) => tv.prettyString + ";;;\n" + prettyType(t)
      case ExpectWellTypedNoValue(t) => "NOINTERPRET\n;;;\n" + prettyType(t)
    }
  }

  def prettyFullTest(ft: FullTest): String = {
    prettyProgram(ft.p) + "\n" + prettyTestExpect(ft.res)
  }

  def usage() {
    println("Takes the name of a .simplescala_sexp test file")
  }

  def main(args: Array[String]) {
    if (args.length != 1) {
      usage()
    } else {
      val contents =
        SimpleScalaParser.fileContents(new java.io.File(args(0)))
      SimpleScalaSExpParser.parseFullTest(args(0), contents) match {
        case Left(error) => println("Could not parse: " + error)
        case Right(test) => println(prettyFullTest(test))
      }
    }
  } // main
} // Printer

