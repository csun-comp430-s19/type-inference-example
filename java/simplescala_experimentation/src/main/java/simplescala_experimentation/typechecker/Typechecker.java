package simplescala_experimentation.typechecker;

import simplescala_experimentation.util.*;
import simplescala_experimentation.syntax.*;

public class Typechecker {
    public final ImmutableMap<FunctionName, NamedFunctionDef> fdefs;
    public final ImmutableMap<UserDefinedTypeName, TypeDef> tdefs;
    public final ImmutableMap<ConstructorName, UserDefinedTypeName> cdefs;

    public Typechecker(final ImmutableMap<FunctionName, NamedFunctionDef> fdefs,
                       final ImmutableMap<UserDefinedTypeName, TypeDef> tdefs,
                       final ImmutableMap<ConstructorName, UserDefinedTypeName> cdefs) {
        this.fdefs = fdefs;
        this.tdefs = tdefs;
        this.cdefs = cdefs;
    }

    public static <A> A getOrElseIllTyped(final Option<A> op) throws IllTyped {
        if (op.isDefined()) {
            return op.get();
        } else {
            throw new IllTyped();
        }
    }
    
    public static <K, V> V getOrElseIllTyped(final ImmutableMap<K, V> map, final K key)
        throws IllTyped {
        return getOrElseIllTyped(map.get(key));
    }
    
    public static <T extends Type> ImmutableList<Type> toTypes(final ImmutableList<T> types) {
        return types.map(new Function1<T, Type>() {
                public Type apply(final T type) {
                    return type;
                }
            });
    }

    public class FreshenCnResult {
        public final UserType userType;
        public final Type type;
        public final State state;

        public FreshenCnResult(final UserType userType,
                               final Type type,
                               final State state) {
            this.userType = userType;
            this.type = type;
            this.state = state;
        }
    }

    public class FreshenResult {
        public final ImmutableList<TypePlaceholder> placeholders;
        public final ImmutableList<Type> types;
        public final State state;

        public FreshenResult(final ImmutableList<TypePlaceholder> placeholders,
                             final ImmutableList<Type> types,
                             final State state) {
            this.placeholders = placeholders;
            this.types = types;
            this.state = state;
        }
    }

    public class FreshenFnResult {
        public final Type paramType;
        public final Type returnType;
        public final State state;

        public FreshenFnResult(final Type paramType,
                               final Type returnType,
                               final State state) {
            this.paramType = paramType;
            this.returnType = returnType;
            this.state = state;
        }
    }

    public class UserTypeTemplateResult {
        public final UserType userType;
        public final ImmutableMap<ConstructorName, Type> mapping;
        public final State state;

        public UserTypeTemplateResult(final UserType userType,
                                      final ImmutableMap<ConstructorName, Type> mapping,
                                      final State state) {
            this.userType = userType;
            this.mapping = mapping;
            this.state = state;
        }
    }

    // threaded state
    public class State {
        public final ConstraintStore cs;
        public final int i;

        public State(final ConstraintStore cs, final int i) {
            this.cs = cs;
            this.i = i;
        }

        public Pair<TypePlaceholder, State> freshPlaceholder() {
            return new Pair<TypePlaceholder, State>(new TypePlaceholder(i),
                                                    new State(cs, i + 1));
        }

        public Pair<ImmutableList<TypePlaceholder>, State> typeListTemplate(final int numVars) {
            final int nextAvailableId = i + numVars;
            final ImmutableList<TypePlaceholder> placeholders =
                Range.until(i, nextAvailableId).map(new Function1<Integer, TypePlaceholder>() {
                        public TypePlaceholder apply(final Integer i) {
                            return new TypePlaceholder(i.intValue());
                        }
                    });
            return new Pair<ImmutableList<TypePlaceholder>, State>(placeholders,
                                                                   new State(cs, nextAvailableId));
        }

        private class MakeFreshVisitor implements TypeVisitor<JavaUnit, Type, IllTyped> {
            public final ImmutableMap<TypeVariable, TypePlaceholder> mapping;

            public MakeFreshVisitor(final ImmutableMap<TypeVariable, TypePlaceholder> mapping) {
                this.mapping = mapping;
            }

            public ImmutableList<Type> visitTypes(final ImmutableList<Type> types) throws IllTyped {
                final MakeFreshVisitor visitor = this;
                return types.map(new Function1<Type, Type>() {
                        public Type apply(final Type input) {
                            return input.visitType(visitor, JavaUnit.UNIT);
                        }
                    });
            }
            
            public Type visit(final StringType t, final JavaUnit down) throws IllTyped { return t; }
            public Type visit(final BooleanType t, final JavaUnit down) throws IllTyped { return t; }
            public Type visit(final IntegerType t, final JavaUnit down) throws IllTyped { return t; }
            public Type visit(final UnitType t, final JavaUnit down) throws IllTyped { return t; }
            public Type visit(final FunctionType ft, final JavaUnit down) throws IllTyped {
                return new FunctionType(ft.paramType.visitType(this, down),
                                        ft.returnType.visitType(this, down));
            }
            public Type visit(final TupleType tt, final JavaUnit down) throws IllTyped {
                return new TupleType(visitTypes(tt.types));
            }
            public Type visit(final UserType ut, final JavaUnit down) throws IllTyped {
                return new UserType(ut.un, visitTypes(ut.types));
            }
            public Type visit(final TypeVariableType tv, final JavaUnit down) throws IllTyped {
                return getOrElseIllTyped(mapping, tv.tv);
            }
            public Type visit(final TypePlaceholder tp, final JavaUnit down) throws IllTyped {
                assert(false);
                return tp;
            }
        } // MakeFreshVisitor
        
        public FreshenResult freshen(final ImmutableList<TypeVariable> typeVars,
                                     final ImmutableList<Type> types) {
            final Pair<ImmutableList<TypePlaceholder>, State> pair =
                typeListTemplate(typeVars.size());
            final ImmutableMap<TypeVariable, TypePlaceholder> mapping =
                new ImmutableMap<TypeVariable, TypePlaceholder>(typeVars.zip(pair.first));

            return new FreshenResult(pair.first,
                                     new MakeFreshVisitor(mapping).visitTypes(types),
                                     pair.second);
        }
        
        public FreshenCnResult freshenCn(final ConstructorName cn) throws IllTyped {
            final UserDefinedTypeName un = getOrElseIllTyped(cdefs, cn);
            final TypeDef typeDef = getOrElseIllTyped(tdefs, un);
            final Type rawParam = getOrElseIllTyped(typeDef.cdefs, cn);
            final FreshenResult freshenResult =
                freshen(typeDef.tvs,
                        new Cons<Type>(rawParam, new Nil<Type>()));

            return new FreshenCnResult(new UserType(un, toTypes(freshenResult.placeholders)),
                                       freshenResult.types.first(),
                                       freshenResult.state);
        } // freshenCn

        public FreshenFnResult freshenFn(final FunctionName fn) throws IllTyped {
            final NamedFunctionDef namedFunctionDef = getOrElseIllTyped(fdefs, fn);
            final FreshenResult freshenResult =
                freshen(namedFunctionDef.tvs,
                        ImmutableList.makeList(namedFunctionDef.paramType,
                                               namedFunctionDef.returnType));
            return new FreshenFnResult(freshenResult.types.first(),
                                       freshenResult.types.rest().first(),
                                       freshenResult.state);
        } // freshenFn

        public Pair<TupleType, State> tupleTemplate(final int numVars) throws IllTyped {
            if (numVars > 1) {
                final Pair<ImmutableList<TypePlaceholder>, State> pair = typeListTemplate(numVars);
                return new Pair<TupleType, State>(new TupleType(toTypes(pair.first)),
                                                  pair.second);
            } else {
                throw new IllTyped();
            }
        } // tupleTemplate

        public class ConstructorCaseVisitor implements CaseVisitor<JavaUnit, ConstructorName, IllTyped> {
            public ConstructorName visit(final ConstructorCase c, final JavaUnit down) throws IllTyped {
                return c.cn;
            }
            public ConstructorName visit(final TupCase tc, final JavaUnit down) throws IllTyped {
                throw new IllTyped();
            }
        }
        
        public UserTypeTemplateResult userTypeTemplate(final ImmutableList<Case> cases) throws IllTyped {
            final ConstructorCaseVisitor constructorCaseVisitor = new ConstructorCaseVisitor();
            final ImmutableList<ConstructorName> consNamesSeq =
                cases.map(new Function1<Case, ConstructorName>() {
                        public ConstructorName apply(final Case input) {
                            return input.visitCase(constructorCaseVisitor, JavaUnit.UNIT);
                        }
                    });
            final ImmutableSet<ConstructorName> consNames = consNamesSeq.toSet();
            if (consNamesSeq.size() != consNames.size()) {
                throw new IllTyped();
            }

            // look for the typedef which has these constructors associated with it
            final Pair<UserDefinedTypeName, TypeDef> targetTypedef =
                getOrElseIllTyped(tdefs.find(new Predicate<Pair<UserDefinedTypeName, TypeDef>>() {
                        public boolean matches(final Pair<UserDefinedTypeName, TypeDef> pair) {
                            return pair.second.cdefs.keySet().equals(consNames);
                        }
                    }));

            final Pair<ImmutableList<ConstructorName>, ImmutableList<Type>> consNamesTypes =
                ImmutableList.unzip(targetTypedef.second.cdefs.pairs);
            final FreshenResult freshenResult = freshen(targetTypedef.second.tvs, consNamesTypes.second);
            final ImmutableMap<ConstructorName, Type> finalMap =
                new ImmutableMap<ConstructorName, Type>(consNamesTypes.first.zip(freshenResult.types));

            return new UserTypeTemplateResult(new UserType(targetTypedef.first,
                                                           toTypes(freshenResult.placeholders)),
                                              finalMap,
                                              freshenResult.state);
        } // userTypeTemplate

        public State unify(final Type t1, final Type t2) throws IllTyped {
            return new State(cs.unify(t1, t2), i);
        } // unify

        public Type lookupTypeFull(final Type type) throws IllTyped {
            return cs.lookupTypeFull(type);
        }
    } // State

    public Pair<ImmutableList<Type>, State> typeofSeq(final ImmutableList<Exp> exps,
                                                      final ImmutableMap<Variable, Type> env,
                                                      State state) throws IllTyped {
        ImmutableList<Type> retval = new Nil<Type>();
        for (final Exp e : exps) {
            final Pair<Type, State> pair = typeof(e, env, state);
            retval = new Cons<Type>(pair.first, retval);
            state = pair.second;
        }
        return new Pair<ImmutableList<Type>, State>(retval.reverse(), state);
    } // typeofSeq

    public Pair<Type, State> typeofBlock(final ImmutableList<Val> vals,
                                         final Exp body,
                                         ImmutableMap<Variable, Type> env,
                                         State state) throws IllTyped {
        for (final Val val : vals) {
            final Pair<Type, State> pair = typeof(val.e, env, state);
            env = env.add(val.x, pair.first);
            state = pair.second;
        }

        return typeof(body, env, state);
    } // typeofBlock

    public class BinopTypesResult {
        public final Type e1Type;
        public final Type e2Type;
        public final Type returnType;

        public BinopTypesResult(final Type e1Type,
                                final Type e2Type,
                                final Type returnType) {
            this.e1Type = e1Type;
            this.e2Type = e2Type;
            this.returnType = returnType;
        }
    } // BinopTypesResult

    public BinopTypesResult binopTypes(final Binop binop) {
        switch (binop) {
        case PLUS:
        case MINUS:
        case TIMES:
        case DIV:
            return new BinopTypesResult(new IntegerType(),
                                        new IntegerType(),
                                        new IntegerType());
        case AND:
        case OR:
            return new BinopTypesResult(new BooleanType(),
                                        new BooleanType(),
                                        new BooleanType());
        case LT:
        case LTE:
            return new BinopTypesResult(new IntegerType(),
                                        new IntegerType(),
                                        new BooleanType());
        case CONCAT:
            return new BinopTypesResult(new StringType(),
                                        new StringType(),
                                        new StringType());
        default:
            assert(false);
            return null;
        }
    } // binopTypes
    
    class TypeofExpVisitor implements ExpVisitor<Pair<ImmutableMap<Variable, Type>, State>, Pair<Type, State>, IllTyped> {
        public Pair<Type, State> visit(final VariableExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            return new Pair<Type, State>(getOrElseIllTyped(down.first, e.x),
                                         down.second);
        }
        public Pair<Type, State> visit(final StringExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            return new Pair<Type, State>(new StringType(), down.second);
        }
        public Pair<Type, State> visit(final BooleanExp b,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            return new Pair<Type, State>(new BooleanType(), down.second);
        }
        public Pair<Type, State> visit(final IntExp i,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            return new Pair<Type, State>(new IntegerType(), down.second);
        }
        public Pair<Type, State> visit(final UnitExp u,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            return new Pair<Type, State>(new UnitType(), down.second);
        }
        public Pair<Type, State> visit(final BinopExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            final BinopTypesResult binopTypesResult = binopTypes(e.op);
            final Pair<Type, State> leftPair = e.e1.visitExp(this, down);
            final State state1 = leftPair.second.unify(binopTypesResult.e1Type, leftPair.first);
            final Pair<Type, State> rightPair = e.e2.visitExp(this, new Pair<ImmutableMap<Variable, Type>, State>(down.first, state1));
            final State state2 = rightPair.second.unify(binopTypesResult.e2Type, rightPair.first);
            return new Pair<Type, State>(binopTypesResult.returnType, state2);
        }
        public Pair<Type, State> visit(final FunctionExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            final Pair<TypePlaceholder, State> freshResult = down.second.freshPlaceholder();
            final Pair<Type, State> eResult =
                e.e.visitExp(this,
                             new Pair<ImmutableMap<Variable, Type>, State>(down.first.add(e.x, freshResult.first),
                                                                           freshResult.second));
            return new Pair<Type, State>(new FunctionType(freshResult.first, eResult.first),
                                         eResult.second);
        }
        public Pair<Type, State> visit(final AnonCallExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            final Pair<Type, State> e1Pair = e.e1.visitExp(this, down);
            final Pair<TypePlaceholder, State> paramBuild = e1Pair.second.freshPlaceholder();
            final Pair<TypePlaceholder, State> retBuild = paramBuild.second.freshPlaceholder();
            final State state5 = retBuild.second.unify(e1Pair.first,
                                                       new FunctionType(paramBuild.first,
                                                                        retBuild.first));
            final Pair<Type, State> e2Pair =
                e.e2.visitExp(this,
                              new Pair<ImmutableMap<Variable, Type>, State>(down.first, state5));
            return new Pair<Type, State>(retBuild.first,
                                         e2Pair.second.unify(e2Pair.first, paramBuild.first));
        }
        public Pair<Type, State> visit(final NamedCallExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            final FreshenFnResult freshenFnResult = down.second.freshenFn(e.fn);
            final Pair<Type, State> ePair =
                e.e.visitExp(this,
                             new Pair<ImmutableMap<Variable, Type>, State>(down.first,
                                                                           freshenFnResult.state));
            return new Pair<Type, State>(freshenFnResult.returnType,
                                         ePair.second.unify(ePair.first, freshenFnResult.paramType));
        }
        public Pair<Type, State> visit(final IfExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            final Pair<Type, State> e1Pair = e.e1.visitExp(this, down);
            final State state3 = e1Pair.second.unify(e1Pair.first, new BooleanType());
            final Pair<Type, State> e2Pair =
                e.e2.visitExp(this,
                              new Pair<ImmutableMap<Variable, Type>, State>(down.first, state3));
            final Pair<Type, State> e3Pair =
                e.e3.visitExp(this,
                              new Pair<ImmutableMap<Variable, Type>, State>(down.first, e2Pair.second));
            return new Pair<Type, State>(e2Pair.first,
                                         e3Pair.second.unify(e2Pair.first, e3Pair.first));
        }
        public Pair<Type, State> visit(final BlockExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            if (e.vals.size() > 0) {
                return typeofBlock(e.vals, e.e, down.first, down.second);
            } else {
                throw new IllTyped();
            }
        }
        public Pair<Type, State> visit(final TupleExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            if (e.es.size() > 1) {
                final Pair<ImmutableList<Type>, State> esPair = typeofSeq(e.es, down.first, down.second);
                return new Pair<Type, State>(new TupleType(esPair.first), esPair.second);
            } else {
                throw new IllTyped();
            }
        }
        public Pair<Type, State> visit(final ConstructorExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            final FreshenCnResult freshenCnResult = down.second.freshenCn(e.cn);
            final Pair<Type, State> ePair =
                e.e.visitExp(this,
                             new Pair<ImmutableMap<Variable, Type>, State>(down.first,
                                                                           freshenCnResult.state));
            return new Pair<Type, State>(freshenCnResult.userType,
                                         ePair.second.unify(ePair.first, freshenCnResult.type));
        }
        public Pair<Type, State> visit(final MatchExp e,
                                       final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            if (e.cases.size() == 1 &&
                e.cases.first() instanceof TupCase) {
                return typeofMatchTup(e.e,
                                      (TupCase)e.cases.first(),
                                      down.first,
                                      down.second);
            } else {
                return typeofMatchConstructors(e.e, e.cases, down.first, down.second);
            }
        }
    } // TypeofExpVisitor

    public Pair<Type, State> typeofMatchTup(final Exp matchOn,
                                            final TupCase tupCase,
                                            final ImmutableMap<Variable, Type> env,
                                            final State state) throws IllTyped {
        if (tupCase.xs.size() != tupCase.xs.toSet().size()) {
            throw new IllTyped();
        }

        final Pair<Type, State> matchOnPair = typeof(matchOn, env, state);
        final Pair<TupleType, State> template = matchOnPair.second.tupleTemplate(tupCase.xs.size());
        final State state4 = template.second.unify(matchOnPair.first, template.first);
        final ImmutableMap<Variable, Type> bodyEnv = env.multiAdd(tupCase.xs.zip(template.first.types));
        return typeof(tupCase.e, bodyEnv, state4);
    }

    public class ConstructorCaseVisitor implements CaseVisitor<Pair<ImmutableMap<Variable, Type>, State>, State, IllTyped> {
        public final ImmutableMap<ConstructorName, Type> mapping;
        public final Type returnType;
        public ConstructorCaseVisitor(final ImmutableMap<ConstructorName, Type> mapping, final Type returnType) {
            this.mapping = mapping;
            this.returnType = returnType;
        }
        public State visit(final ConstructorCase c,
                           final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            final Pair<Type, State> ePair = typeof(c.e,
                                                   down.first.add(c.x, getOrElseIllTyped(mapping, c.cn)),
                                                   down.second);
            return ePair.second.unify(returnType, ePair.first);
        }
        public State visit(final TupCase c,
                           final Pair<ImmutableMap<Variable, Type>, State> down) throws IllTyped {
            throw new IllTyped();
        }
    }
    
    public Pair<Type, State> typeofMatchConstructors(final Exp matchOn,
                                                     final ImmutableList<Case> cases,
                                                     final ImmutableMap<Variable, Type> env,
                                                     final State state) throws IllTyped {
        final UserTypeTemplateResult userTypeTemplateResult =
            state.userTypeTemplate(cases);
        final Pair<Type, State> matchOnPair = typeof(matchOn, env, userTypeTemplateResult.state);
        final State state4 = matchOnPair.second.unify(userTypeTemplateResult.userType, matchOnPair.first);
        final Pair<TypePlaceholder, State> placeholderPair = state4.freshPlaceholder();
        final ConstructorCaseVisitor visitor = new ConstructorCaseVisitor(userTypeTemplateResult.mapping,
                                                                          placeholderPair.first);
        State curState = placeholderPair.second;
        for (final Case curCase : cases) {
            curState = curCase.visitCase(visitor,
                                         new Pair<ImmutableMap<Variable, Type>, State>(env, curState));
        }
        return new Pair<Type, State>(placeholderPair.first, curState);
    } // typeofMatchConstructors

    public Pair<Type, State> typeof(final Exp e,
                                    final ImmutableMap<Variable, Type> env,
                                    final State state) throws IllTyped {
        return e.visitExp(new TypeofExpVisitor(),
                          new Pair<ImmutableMap<Variable, Type>, State>(env, state));
    }

    public State initialState() {
        return new State(new ConstraintStore(), 0);
    }
    
    public void checkDef(final Def def) throws IllTyped {
        final ImmutableMap<Variable, Type> emptyMap = new ImmutableMap<Variable, Type>();
        final Pair<Type, State> ePair = typeof(def.e,
                                               emptyMap.add(def.x, def.tau1),
                                               initialState());
        ePair.second.unify(def.tau2, ePair.first);
    }

    public Type typeof(final Exp e) throws IllTyped {
        final Pair<Type, State> ePair = typeof(e,
                                               new ImmutableMap<Variable, Type>(),
                                               initialState());
        return ePair.second.lookupTypeFull(ePair.first);
    }

    public static <A> void ensureSet(final ImmutableList<A> items) throws IllTyped {
        if (items.size() != items.toSet().size()) {
            throw new IllTyped();
        }
    }

    public static void ensureTypeVarsInScope(final ImmutableList<TypeVariable> typeVars, final Type type) throws IllTyped {
        type.visitType(new EnsureTypeVarsInScopeVisitor(typeVars.toSet()),
                       JavaUnit.UNIT);
    }

    public static void ensureAllFunctionsUnique(final Program p) throws IllTyped {
        ensureSet(p.defs.map(new Function1<Def, FunctionName>() {
                public FunctionName apply(final Def def) {
                    return def.fn;
                }
            }));
    }

    public static void ensureAllTypesUnique(final Program p) throws IllTyped {
        ensureSet(p.tdefs.map(new Function1<UserDefinedTypeDef, UserDefinedTypeName>() {
                public UserDefinedTypeName apply(final UserDefinedTypeDef tdef) {
                    return tdef.un;
                }
            }));
    }
    
    // Each should be a set, and they should be in scope
    public static void ensureAllTypeVarsOk(final Program p) throws IllTyped {
        for (final UserDefinedTypeDef tdef : p.tdefs) {
            final ImmutableList<TypeVariable> typeVars = tdef.ts;
            ensureSet(typeVars);
            for (final ConstructorDefinition cdef : tdef.cdefs) {
                ensureTypeVarsInScope(typeVars, cdef.tau);
            }
        }
        
        for (final Def def : p.defs) {
            final ImmutableList<TypeVariable> typeVars = def.ts;
            ensureSet(typeVars);
            ensureTypeVarsInScope(typeVars, def.tau1);
            ensureTypeVarsInScope(typeVars, def.tau2);
        }
    }

    public static ImmutableMap<FunctionName, NamedFunctionDef> makeFDefs(final Program p) {
        final ImmutableList<Pair<FunctionName, NamedFunctionDef>> pairs =
            p.defs.map(new Function1<Def, Pair<FunctionName, NamedFunctionDef>>() {
                    public Pair<FunctionName, NamedFunctionDef> apply(final Def def) {
                        return new Pair<FunctionName, NamedFunctionDef>(def.fn,
                                                                        new NamedFunctionDef(def.ts,
                                                                                             def.tau1,
                                                                                             def.tau2));
                    }
                });
        return new ImmutableMap<FunctionName, NamedFunctionDef>(pairs);
    }

    public static ImmutableMap<UserDefinedTypeName, TypeDef> makeTDefs(final Program p) {
        final ImmutableList<Pair<UserDefinedTypeName, TypeDef>> pairs =
            p.tdefs.map(new Function1<UserDefinedTypeDef, Pair<UserDefinedTypeName, TypeDef>>() {
                    public Pair<UserDefinedTypeName, TypeDef> apply(final UserDefinedTypeDef input) {
                        final ImmutableList<Pair<ConstructorName, Type>> constructorPairs =
                        input.cdefs.map(new Function1<ConstructorDefinition, Pair<ConstructorName, Type>>() {
                                public Pair<ConstructorName, Type> apply(final ConstructorDefinition cdef) {
                                    return new Pair<ConstructorName, Type>(cdef.cn, cdef.tau);
                                }
                            });
                        final TypeDef typeDef = new TypeDef(input.ts,
                                                            new ImmutableMap<ConstructorName, Type>(constructorPairs));
                        return new Pair<UserDefinedTypeName, TypeDef>(input.un, typeDef);
                    }
                });
        return new ImmutableMap<UserDefinedTypeName, TypeDef>(pairs);
    }

    public static ImmutableMap<ConstructorName, UserDefinedTypeName> makeCDefs(final Program p) {
        final ImmutableList<Pair<ConstructorName, UserDefinedTypeName>> pairs =
            p.tdefs.flatMap(new Function1<UserDefinedTypeDef, ImmutableList<Pair<ConstructorName, UserDefinedTypeName>>>() {
                    public ImmutableList<Pair<ConstructorName, UserDefinedTypeName>> apply(final UserDefinedTypeDef tdef) {
                        return tdef.cdefs.map(new Function1<ConstructorDefinition, Pair<ConstructorName, UserDefinedTypeName>>() {
                                public Pair<ConstructorName, UserDefinedTypeName> apply(final ConstructorDefinition cdef) {
                                    return new Pair<ConstructorName, UserDefinedTypeName>(cdef.cn, tdef.un);
                                }
                            });
                    }
                });
        return new ImmutableMap<ConstructorName, UserDefinedTypeName>(pairs);
    }
    
    public static Type programType(final Program p) throws IllTyped {
        ensureAllFunctionsUnique(p);
        ensureAllTypesUnique(p);
        ensureAllTypeVarsOk(p);
        final Typechecker checker = new Typechecker(makeFDefs(p),
                                                    makeTDefs(p),
                                                    makeCDefs(p));
        for (final Def def : p.defs) {
            checker.checkDef(def);
        }
        return checker.typeof(p.e);
    }

    // isomorphic means that they vary only by the actual ids
    // used for the placeholders.  This means that not only
    // should the two types unify, placeholders should only ever
    // be unified with other placeholders.
    public static boolean typesIsomorphic(final Type t1, final Type t2) {
        try {
            final ConstraintStore store1 = new ConstraintStore();
            final ConstraintStore store2 = store1.unify(t1, t2);
            return store2.map.keySet().toList().forall(new Predicate<TypePlaceholder>() {
                    public boolean matches(final TypePlaceholder key) {
                        return store2.lookup(key) instanceof TypePlaceholder;
                    }
                });
        } catch (final IllTyped e) {
            return false;
        }
    }

    public static Option<Type> opProgramType(final Program p) {
        try {
            return new Some<Type>(programType(p));
        } catch (final IllTyped e) {
            // e.printStackTrace();
            return new None<Type>();
        }
    }
        
    // TODO: STOPPED HERE
    // - Need s-expression parser ported
    // - Need converter for s-expressions to ASTs ported
    // - Need test harness ported
    // - Need to actually compare the two test suites via mutation testing
    // - Need to writeup the results
} // Typechecker
