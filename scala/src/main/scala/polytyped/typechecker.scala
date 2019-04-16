package simplescala_experimentation.polytyped

class IllTyped(msg: String) extends Exception(msg) {
  def this() {
    this("")
  }
}

object Aliases {
  type NamedFunctionDefs = Map[FunctionName, (Seq[TypeVariable], Type, Type)]
  type TypeDefs = Map[UserDefinedTypeName, (Seq[TypeVariable], Map[ConstructorName, Type])]
  type ConstructorDefs = Map[ConstructorName, UserDefinedTypeName]
  type TypeEnv = Map[Variable, Type]
}
import Aliases._

case class ConstraintStore(val map: Map[TypePlaceholder, Type]) {
  @scala.annotation.tailrec
  final def lookup(p: TypePlaceholder): Type = {
    map.get(p) match {
      case Some(otherP: TypePlaceholder) => lookup(otherP)
      case Some(other) => other
      case None => p
    }
  }

  def lookupTypeSingleLevel(typ: Type): Type = {
    typ match {
      case p: TypePlaceholder => lookup(p)
      case _ => typ
    }
  }

  def lookupTypesFull(types: List[Type]): List[Type] = {
    types.map(lookupTypeFull)
  }

  def lookupTypeFull(typ: Type): Type = {
    typ match {
      case StringType | BooleanType | IntegerType | UnitType | TypeVariableType(_) => typ
      case FunctionType(t1, t2) => FunctionType(lookupTypeFull(t1), lookupTypeFull(t2))
      case TupleType(types) => TupleType(lookupTypesFull(types))
      case UserType(un, types) => UserType(un, lookupTypesFull(types))
      case p: TypePlaceholder => {
        if (map.contains(p)) {
          lookupTypeFull(lookup(p))
        } else {
          p
        }
      }
    }
  }
            
  private def typesContains(types: Seq[Type], p: TypePlaceholder): Boolean = {
    types.exists(typ => typeContains(typ, p))
  }

  private def typeContains(typ: Type, p: TypePlaceholder): Boolean = {
    typ match {
      case StringType | BooleanType | IntegerType | UnitType | TypeVariableType(_) => false
      case FunctionType(t1, t2) => typesContains(Seq(t1, t2), p)
      case TupleType(types) => typesContains(types, p)
      case UserType(_, types) => typesContains(types, p)
      case otherP: TypePlaceholder => {
        lookup(otherP) match {
          case `p` => true // same placeholder
          case TypePlaceholder(_) => false // other placeholder
          case otherLook => typeContains(otherLook, p)
        }
      }
    }
  }

  private def unifyPlaceholderType(p: TypePlaceholder, typ: Type): Option[ConstraintStore] = {
    if (p == typ) {
      Some(this)
    } else if (!typeContains(typ, p)) {
      Some(ConstraintStore(map + (p -> typ)))
    } else {
      None
    }
  }

  def unify(t1s: Seq[Type], t2s: Seq[Type]): Option[ConstraintStore] = {
    if (t1s.size != t2s.size) {
      None
    } else {
      t1s.zip(t2s).foldLeft(Some(this): Option[ConstraintStore])((res, cur) =>
        res.flatMap(_.unify(cur._1, cur._2)))
    }
  }

  def unify(t1: Type, t2: Type): Option[ConstraintStore] = {
    val lookT1 = lookupTypeSingleLevel(t1)
    val lookT2 = lookupTypeSingleLevel(t2)
    (lookT1, lookT2) match {
      case (p: TypePlaceholder, other) => {
        unifyPlaceholderType(p, other)
      }
      case (other, p: TypePlaceholder) => {
        unifyPlaceholderType(p, other)
      }
      case (StringType, StringType) | (BooleanType, BooleanType) | (IntegerType, IntegerType) | (UnitType, UnitType) => Some(this)
      case (TypeVariableType(t1), TypeVariableType(t2)) if t1 == t2 => Some(this)
      case (FunctionType(t1_1, t1_2), FunctionType(t2_1, t2_2)) => {
        unify(Seq(t1_1, t1_2), Seq(t2_1, t2_2))
      }
      case (TupleType(types1), TupleType(types2)) => {
        unify(types1, types2)
      }
      case (UserType(un1, types1), UserType(un2, types2)) if un1 == un2 => {
        unify(types1, types2)
      }
      case _ => None
    }
  }
} // ConstraintStore

object Typechecker {
  def ensureSet[A](items: Seq[A]) {
    if (items.toSet.size != items.size) {
      throw new IllTyped
    }
  }

  def ensureTypeVarsInScope(ts: Seq[TypeVariable], tau: Type) {
    val asSet = ts.toSet
    def recur(tau: Type) {
      tau match {
        case StringType | BooleanType | IntegerType | UnitType => ()
        case FunctionType(tau1, tau2) => {
          recur(tau1)
          recur(tau2)
        }
        case TupleType(taus) => taus.foreach(recur)
        case UserType(_, taus) => taus.foreach(recur)
        case TypeVariableType(t) if asSet.contains(t) => ()
        case _ => throw new IllTyped
      }
    } // recur

    recur(tau)
  } // ensureTypeVarsInScope

  def apply(p: Program): Type = {
    ensureSet(p.defs.map(_.fn))
    p.defs.foreach(d => ensureSet(d.ts))
    ensureSet(p.tdefs.map(_.un))
    ensureSet(p.tdefs.flatMap(_.cdefs.map(_.cn)))
    p.tdefs.foreach(td => ensureSet(td.ts))
    p.tdefs.foreach(td =>
      td.cdefs.foreach(cd =>
        ensureTypeVarsInScope(td.ts, cd.tau)))
    p.defs.foreach(d => {
      ensureTypeVarsInScope(d.ts, d.tau1)
      ensureTypeVarsInScope(d.ts, d.tau2)
    })
      
    val fdefs = p.defs.map( { case Def(fn, ts, _, tau1, tau2, _) => (fn -> (ts, tau1, tau2)) } ).toMap
    val tdefs = p.tdefs.map(
      { case UserDefinedTypeDef(un, ts, cdefs) =>
          (un -> (ts -> cdefs.map(
            { case ConstructorDefinition(cn, tau) => (cn -> tau) }).toMap)) }).toMap
    val cdefs = p.tdefs.flatMap(
      { case UserDefinedTypeDef(un, _, cdefs) => cdefs.map(
          { case ConstructorDefinition(cn, _) => (cn -> un) } ) } ).toMap
    val checker = new Typechecker(fdefs, tdefs, cdefs)
    p.defs.foreach(checker.checkDef)
    checker.typeof(p.e)
  } // apply

  def eitherType(p: Program): Either[IllTyped, Type] = {
    try {
      Right(apply(p))
    } catch {
      case e: IllTyped => Left(e)
    }
  }

  def lift[A](op: Option[A], msg: String = ""): A = {
    op.getOrElse(throw new IllTyped(msg))
  }

  def binopTypes(binop: Binop): ((Type, Type), Type) = {
    binop match {
      case BinopPlus | BinopMinus | BinopTimes | BinopDiv => ((IntegerType -> IntegerType) -> IntegerType)
      case BinopAnd | BinopOr => ((BooleanType -> BooleanType) -> BooleanType)
      case BinopLT | BinopLTE => ((IntegerType -> IntegerType) -> BooleanType)
      case BinopConcat => ((StringType -> StringType) -> StringType)
    }
  }
} // Typechecker

class Typechecker(val fdefs: NamedFunctionDefs,
                  val tdefs: TypeDefs,
                  val cdefs: ConstructorDefs) {
  import Typechecker._

  private val initialState = State(ConstraintStore(Map()), 0)

  // threaded state
  case class State(cs: ConstraintStore, i: Int) {
    // Returns:
    // - Corresponding user-defined type
    // - Constructor parameter type
    // - Next state
    def freshenCn(cn: ConstructorName): (UserType, Type, State) = {
      val un = cdefs.getOrElse(cn, throw new IllTyped)
      val (typeVars, mapping) = tdefs.getOrElse(un, throw new IllTyped)
      val rawParam = mapping.getOrElse(cn, throw new IllTyped)
      val (placeholders, Seq(param), finalState) = freshen(typeVars, Seq(rawParam))
      (UserType(un, placeholders.toList), param, finalState)
    }

    // Returns:
    // - Parameter type to the function
    // - Function's return type
    // - Next state
    def freshenFn(fn: FunctionName): (Type, Type, State) = {
      val (typeVars, rawParam, rawRet) = fdefs.get(fn).getOrElse(throw new IllTyped)
      val (_, Seq(t1, t2), finalState) = freshen(typeVars, Seq(rawParam, rawRet))
      (t1, t2, finalState)
    }

    // Returns:
    // - Placeholders for the type variables
    // - Freshened types which use these placeholders
    // - Next state
    def freshen(typeVars: Seq[TypeVariable], types: Seq[Type]): (Seq[TypePlaceholder], Seq[Type], State) = {
      assert(typeVars.toSet.size == typeVars.size)
      val (placeholders, state2) = typeListTemplate(typeVars.size)
      val mapping: Map[TypeVariable, TypePlaceholder] =
        typeVars.zip(placeholders).toMap
      
      def makeFreshList(types: List[Type]): List[Type] = {
        types.map(makeFresh)
      }

      def makeFresh(typ: Type): Type = {
        typ match {
          case StringType | BooleanType | IntegerType | UnitType => typ
          case FunctionType(t1, t2) => FunctionType(makeFresh(t1), makeFresh(t2))
          case TupleType(types) => TupleType(makeFreshList(types))
          case UserType(un, types) => UserType(un, makeFreshList(types))
          case TypeVariableType(tv) => mapping.get(tv).getOrElse(throw new IllTyped)
          case TypePlaceholder(_) => {
            assert(false)
            typ
          }
        }
      }

      (placeholders, makeFreshList(types.toList).toSeq, state2)
    }

    def typeListTemplate(numVars: Int): (Seq[TypePlaceholder], State) = {
      val nextAvailableId = i + numVars
      (i.until(nextAvailableId).map(TypePlaceholder.apply) -> copy(i = nextAvailableId))
    }

    def tupleTemplate(numVars: Int): (TupleType, State) = {
      if (numVars > 1) {
        val (placeholders, state2) = typeListTemplate(numVars)
        (TupleType(placeholders.toList) -> state2)
      } else {
        throw new IllTyped
      }
    }

    def userTypeTemplate(cases: Seq[Case]): (UserType, Map[ConstructorName, Type], State) = {
      val consNamesSeq: Seq[ConstructorName] =
        cases.map(_ match {
          case ConstructorCase(cn, _, _) => cn
          case TupCase(_, _) => throw new IllTyped
        })
      val consNames: Set[ConstructorName] = consNamesSeq.toSet
      if (consNamesSeq.size != consNames.size) {
        throw new IllTyped
      }
      
      // look for the typedef which has these constructors associated with it
      val (un, typeVars, constructorMap) =
        tdefs.find( { case (_, (_, mapping)) => mapping.keySet == consNames } )
             .map( { case (u, (tv, mapping)) => (u, tv, mapping) } )
             .getOrElse(throw new IllTyped)

      val mapPairs: Seq[(ConstructorName, Type)] = constructorMap.toSeq
      val (placeholders, freshTypes, state2) = freshen(typeVars, mapPairs.map(_._2))
      val finalMap: Map[ConstructorName, Type] = mapPairs.map(_._1).zip(freshTypes).toMap
      (UserType(un, placeholders.toList), finalMap, state2)
    }

    def freshPlaceholder(): (TypePlaceholder, State) = {
      (TypePlaceholder(i) -> copy(i = i + 1))
    }

    def lookupTypeFull(t: Type): Type = {
      cs.lookupTypeFull(t)
    }

    def unify(t1: Type, t2: Type): State = {
      lift(cs.unify(t1, t2).map(newCs => copy(cs = newCs)),
           "INCOMPATIBLE: " + lookupTypeFull(t1) +
           "\nINCOMPATIBLE: " + lookupTypeFull(t2))
    }
  } // State

  def typeofSeq(exps: Seq[Exp], env: TypeEnv, state1: State): (Seq[Type], State) = {
    val (stateFinal, revList) =
      exps.foldLeft((state1 -> List[Type]()))((res, cur) => {
        val (state2, types) = res
        val (curType, state3) = typeof(cur, env, state2)
        (state3 -> (curType :: types))
      })
    (revList.reverse.toSeq -> stateFinal)
  }

  @scala.annotation.tailrec
  final def typeofBlock(vals: List[Val], body: Exp, env: TypeEnv, state1: State): (Type, State) = {
    vals match {
      case Val(x, e) :: rest => {
        val (eType, state2) = typeof(e, env, state1)
        typeofBlock(rest, body, env + (x -> eType), state2)
      }
      case Nil => typeof(body, env, state1)
    }
  }

  def typeof(exp: Exp, env: TypeEnv, state1: State): (Type, State) = {
    exp match {
      case VariableExp(x) => (env.getOrElse(x, throw new IllTyped) -> state1)
      case StringExp(_) => (StringType -> state1)
      case BooleanExp(_) => (BooleanType -> state1)
      case IntExp(_) => (IntegerType -> state1)
      case UnitExp => (UnitType -> state1)
      case BinopExp(e1, binop, e2) => {
        val ((expectedT1, expectedT2), resultType) = binopTypes(binop)
        val (Seq(t1, t2), state2) = typeofSeq(Seq(e1, e2), env, state1)
        (resultType -> state2.unify(t1, expectedT1).unify(t2, expectedT2))
      }
      case FunctionExp(x, e) => {
        val (xType, state2) = state1.freshPlaceholder
        val (eType, state3) = typeof(e, env + (x -> xType), state2)
        (FunctionType(xType, eType) -> state3)
      }
      case AnonCallExp(e1, e2) => {
        val (e1Type, state2) = typeof(e1, env, state1)
        val (fParamType, state3) = state2.freshPlaceholder
        val (fRetType, state4) = state3.freshPlaceholder
        val state5 = state4.unify(e1Type, FunctionType(fParamType, fRetType))
        val (e2Type, state6) = typeof(e2, env, state5)
        (fRetType -> state6.unify(e2Type, fParamType))
      }
      case NamedCallExp(fn, e) => {
        val (pType, retType, state2) = state1.freshenFn(fn)
        val (eType, state3) = typeof(e, env, state2)
        (retType -> state3.unify(eType, pType))
      }
      case IfExp(e1, e2, e3) => {
        val (e1Type, state2) = typeof(e1, env, state1)
        val state3 = state2.unify(e1Type, BooleanType)
        val (e2Type, state4) = typeof(e2, env, state3)
        val (e3Type, state5) = typeof(e3, env, state4)
        (e2Type -> state5.unify(e2Type, e3Type))
      }
      case BlockExp(vals, e) => {
        typeofBlock(vals, e, env, state1)
      }
      case TupleExp(es) => {
        if (es.size > 1) {
          val (types, state2) = typeofSeq(es.toSeq, env, state1)
          (TupleType(types.toList) -> state2)
        } else {
          throw new IllTyped
        }
      }
      case ConstructorExp(cn, e) => {
        val (userType, expectedEType, state2) = state1.freshenCn(cn)
        val (actualEType, state3) = typeof(e, env, state2)
        val state4 = state3.unify(expectedEType, actualEType)
        (userType -> state4)
      }
      case MatchExp(matchOn, TupCase(xs, body) :: Nil) => {
        if (xs.toSet.size != xs.size) {
          throw new IllTyped
        }
        val (matchOnType, state2) = typeof(matchOn, env, state1)
        val (expectedType@TupleType(xTypes), state3) = state2.tupleTemplate(xs.size)
        val state4 = state3.unify(matchOnType, expectedType)
        val bodyEnv = xs.zip(xTypes).foldLeft(env)(_ + _)
        typeof(body, bodyEnv, state4)
      }
      case MatchExp(matchOn, cases) => {
        val (expectedType, mapping, state2) = state1.userTypeTemplate(cases)
        val (actualType, state3) = typeof(matchOn, env, state2)
        val state4 = state3.unify(expectedType, actualType)
        val (returnType, state5) = state4.freshPlaceholder
        val state6 =
          cases.foldLeft(state5)((curState1, curCase) => {
            val ConstructorCase(cn, x, body) = curCase
            val (bodyType, curState2) = typeof(body, env + (x -> mapping.getOrElse(cn, throw new IllTyped)), curState1)
            curState2.unify(returnType, bodyType)
          })
        (returnType -> state6)
      }
    }
  } // typeof

  def checkDef(theDef: Def) {
    val Def(_, _, x, paramType, expectedRetType, e) = theDef
    val (actualRetType, state) = typeof(e, Map(x -> paramType), initialState)
    state.unify(expectedRetType, actualRetType)
  }

  def typeof(exp: Exp): Type = {
    val (retType, state) = typeof(exp, Map(), initialState)
    state.lookupTypeFull(retType)
  }
}
