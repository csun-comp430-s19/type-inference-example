package simplescala_experimentation.polytyped

class SimpleScalaSExpParserException(msg: String) extends Exception(msg)

object SimpleScalaSExpParser {
  def error(msg: String): Nothing = {
    throw new SimpleScalaSExpParserException(msg)
  }

  def error(msg: String, sexp: SExp): Nothing = {
    error(msg + ": " + sexp.asString)
  }

  def parse[A](thing: String, sexp: SExp, pf: PartialFunction[SExp, A]): A = {
    pf.applyOrElse(sexp, (sexp: SExp) => error("Not a " + thing, sexp))
  }

  def parseFDef(sexp: SExp): Def = {
    parse("function defintion", sexp,
          { case SExpList(SExpAtom("fdef") :: fn :: SExpList(tvs) :: x :: t1 :: t2 :: e :: Nil) =>
              Def(parseFunctionName(fn),
                  tvs.map(parseTypeVariable),
                  parseVariable(x),
                  parseType(t1),
                  parseType(t2),
                  parseExp(e)) } )
  }

  def parseProgram(sexp: SExp): Program = {
    parse("program", sexp,
          { case SExpList(SExpAtom("program") :: SExpList(tdefs) :: SExpList(fdefs) :: exp :: Nil) => {
            Program(tdefs.map(parseTDef).toSeq,
                    fdefs.map(parseFDef).toSeq,
                    parseExp(exp))
          }})
  }

  def parseTest(sexp: SExp): String => FullTest = {
    parse("test", sexp,
          { case SExpList(SExpAtom("test") :: p :: t :: Nil) => {
              val prog = parseProgram(p)
              val expect =
                parsePossibleType(t).map(ExpectWellTypedNoValue.apply).getOrElse(ExpectIllTyped)
              (name: String) => FullTest(name, prog, expect) } })
  }

  def parseFullTest(fileName: String, contents: String): Either[String, FullTest] = {
    new SExpParser(contents).parse match {
      case Some(sexp :: Nil) => {
        try {
          Right(parseTest(sexp)(fileName))
        } catch {
          case e: SimpleScalaSExpParserException => {
            Left(e.getMessage)
          }
        }
      }
      case Some(_) => Left("multiple s-expressions in file: " + fileName)
      case None => Left("Invalid s-expression in file: " + fileName)
    }
  }

  def parseVariable(sexp: SExp): Variable = {
    parse("variable", sexp,
          { case SExpList(SExpAtom("variable") :: SExpAtom(name) :: Nil) =>
              Variable(name) })
  }

  def parseFunctionName(sexp: SExp): FunctionName = {
    parse("function name", sexp,
          { case SExpList(SExpAtom("function_name") :: SExpAtom(name) :: Nil) =>
              FunctionName(name) })
  }

  def parseTypeVariable(sexp: SExp): TypeVariable = {
    parse("type variable", sexp,
          { case SExpList(SExpAtom("type_variable_name") :: SExpAtom(name) :: Nil) =>
              TypeVariable(name) })
  }

  def parseUserDefinedTypeName(sexp: SExp): UserDefinedTypeName = {
    parse("type name", sexp,
          { case SExpList(SExpAtom("user_defined_type_name") :: SExpAtom(name) :: Nil) =>
              UserDefinedTypeName(name) })
  }

  def parseCDef(sexp: SExp): ConstructorDefinition = {
    parse("constructor definition", sexp,
          { case SExpList(SExpAtom("cdef") :: cn :: t :: Nil) =>
              ConstructorDefinition(parseConstructorName(cn), parseType(t)) })
  }

  def parseTDef(sexp: SExp): UserDefinedTypeDef = {
    parse("type definition", sexp,
          { case SExpList(SExpAtom("tdef") :: tn :: SExpList(tvs) :: SExpList(cdefs) :: Nil) =>
              UserDefinedTypeDef(parseUserDefinedTypeName(tn),
                                 tvs.map(parseTypeVariable),
                                 cdefs.map(parseCDef).toSeq) })
  }

  def parseConstructorName(sexp: SExp): ConstructorName = {
    parse("constructor name", sexp,
          { case SExpList(SExpAtom("constructor_name") :: SExpAtom(name) :: Nil) =>
              ConstructorName(name) })
  }


  def or[A](thing: String, sexp: SExp, fs: PartialFunction[SExp, A]*): A = {
    parse(thing, sexp, fs.reduceRight(_.orElse(_)))
  }

  def parsePossibleType(sexp: SExp): Option[Type] = {
    or("possible type", sexp,
       { case SExpAtom("ILLTYPED") => None },
       { case other => Some(parseType(other)) })
  }

  def parseType(sexp: SExp): Type = {
    or("type", sexp,
       { case SExpAtom("type_string") => StringType },
       { case SExpAtom("type_boolean") => BooleanType },
       { case SExpAtom("type_integer") => IntegerType },
       { case SExpAtom("type_unit") => UnitType },
       { case SExpList(SExpAtom("type_function") :: t1 :: t2 :: Nil) =>
           FunctionType(parseType(t1), parseType(t2)) },
       { case SExpList(SExpAtom("type_tuple") :: SExpList(types) :: Nil) =>
           TupleType(types.map(parseType)) },
       { case SExpList(SExpAtom("type_user") :: un :: SExpList(types) :: Nil) =>
           UserType(parseUserDefinedTypeName(un), types.map(parseType)) },
       { case SExpList(SExpAtom("type_variable") :: tv :: Nil) =>
           TypeVariableType(parseTypeVariable(tv)) },
       { case SExpList(SExpAtom("type_placeholder") :: SExpAtom(n) :: Nil) =>
           TypePlaceholder(n.toInt) })
  } // parseType

  def inQuotes(s: String): Boolean = {
    val len = s.length
    len >= 2 && s.charAt(0) == '"' && s.charAt(len - 1) == '"'
  }

  def withoutQuotes(s: String): String = {
    assert(inQuotes(s))
    s.substring(1, s.length - 1)
  }

  def parseBinop(sexp: SExp): Binop = {
    or("binop", sexp,
       { case SExpAtom("+") => BinopPlus },
       { case SExpAtom("-") => BinopMinus },
       { case SExpAtom("*") => BinopTimes },
       { case SExpAtom("/") => BinopDiv },
       { case SExpAtom("&&") => BinopAnd },
       { case SExpAtom("||") => BinopOr },
       { case SExpAtom("<") => BinopLT },
       { case SExpAtom("<=") => BinopLTE },
       { case SExpAtom("++") => BinopConcat })
  }

  def parseVal(sexp: SExp): Val = {
    parse("val", sexp,
          { case SExpList(SExpAtom("val") :: x :: e :: Nil) =>
              Val(parseVariable(x), parseExp(e)) })
  }

  def parseCase(sexp: SExp): Case = {
    or("case", sexp,
       { case SExpList(SExpAtom("case_constructor") :: cn :: x :: e :: Nil) =>
           ConstructorCase(parseConstructorName(cn), parseVariable(x), parseExp(e)) },
       { case SExpList(SExpAtom("case_tup") :: SExpList(xs) :: e :: Nil) =>
           TupCase(xs.map(parseVariable), parseExp(e)) })
  }

  def parseExp(sexp: SExp): Exp = {
    or("expression", sexp,
       { case SExpList(SExpAtom("exp_variable") :: x :: Nil) => VariableExp(parseVariable(x)) },
       { case SExpList(SExpAtom("exp_string") :: SExpAtom(string) :: Nil) => StringExp(withoutQuotes(string)) },
       { case SExpList(SExpAtom("exp_boolean") :: SExpAtom(b) :: Nil) => BooleanExp(b.toBoolean) },
       { case SExpList(SExpAtom("exp_int") :: SExpAtom(i) :: Nil) => IntExp(i.toInt) },
       { case SExpAtom("exp_unit") => UnitExp },
       { case SExpList(SExpAtom("exp_binop") :: e1 :: binop :: e2 :: Nil) =>
           BinopExp(parseExp(e1), parseBinop(binop), parseExp(e2)) },
       { case SExpList(SExpAtom("exp_function") :: x :: e :: Nil) =>
           FunctionExp(parseVariable(x), parseExp(e)) },
       { case SExpList(SExpAtom("exp_anoncall") :: e1 :: e2 :: Nil) =>
           AnonCallExp(parseExp(e1), parseExp(e2)) },
       { case SExpList(SExpAtom("exp_namedcall") :: fn :: e :: Nil) =>
           NamedCallExp(parseFunctionName(fn), parseExp(e)) },
       { case SExpList(SExpAtom("exp_if") :: e1 :: e2 :: e3 :: Nil) =>
           IfExp(parseExp(e1), parseExp(e2), parseExp(e3)) },
       { case SExpList(SExpAtom("exp_block") :: SExpList(vals) :: e :: Nil) =>
           BlockExp(vals.map(parseVal), parseExp(e)) },
       { case SExpList(SExpAtom("exp_tuple") :: SExpList(es) :: Nil) =>
           TupleExp(es.map(parseExp)) },
       { case SExpList(SExpAtom("exp_constructor") :: cn :: e :: Nil) =>
           ConstructorExp(parseConstructorName(cn), parseExp(e)) },
       { case SExpList(SExpAtom("exp_match") :: e :: SExpList(cases) :: Nil) =>
           MatchExp(parseExp(e), cases.map(parseCase)) })
  }
} // SimpleScalaSExpParser
