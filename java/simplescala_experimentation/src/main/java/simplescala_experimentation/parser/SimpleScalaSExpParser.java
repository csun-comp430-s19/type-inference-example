package simplescala_experimentation.parser;

import simplescala_experimentation.util.*;
import simplescala_experimentation.syntax.*;

public class SimpleScalaSExpParser {
    public static <A> A parseSingleWrapped(final SExp sexp,
                                           final String tag,
                                           final Function1<String, A> converter) {
        final CaptureAtomPattern name = new CaptureAtomPattern();
        return new ConstantListPattern(new ConstantAtomPattern(tag),
                                       name).ifMatches(sexp,
                                                       new Function0<A>() {
                                                           public A apply() {
                                                               return converter.apply(name.getCapture());
                                                           }
                                                       });
    }
    public static Variable parseVariable(final SExp sexp) {
        return parseSingleWrapped(sexp,
                                  "variable",
                                  new Function1<String, Variable>() {
                                      public Variable apply(final String name) {
                                          return new Variable(name);
                                      }
                                  });
    }

    public static ImmutableList<Variable> parseVariables(final ImmutableList<SExp> sexps) {
        return sexps.map(new Function1<SExp, Variable>() {
                public Variable apply(final SExp input) {
                    return parseVariable(input);
                }
            });
    }
    
    public static FunctionName parseFunctionName(final SExp sexp) {
        return parseSingleWrapped(sexp,
                                  "function_name",
                                  new Function1<String, FunctionName>() {
                                      public FunctionName apply(final String name) {
                                          return new FunctionName(name);
                                      }
                                  });
    }

    public static TypeVariable parseTypeVariable(final SExp sexp) {
        return parseSingleWrapped(sexp,
                                  "type_variable_name",
                                  new Function1<String, TypeVariable>() {
                                      public TypeVariable apply(final String name) {
                                          return new TypeVariable(name);
                                      }
                                  });
    }

    public static ImmutableList<TypeVariable> parseTypeVariables(final ImmutableList<SExp> list) {
        return list.map(new Function1<SExp, TypeVariable>() {
                public TypeVariable apply(final SExp input) {
                    return parseTypeVariable(input);
                }
            });
    }
    
    public static UserDefinedTypeName parseUserDefinedTypeName(final SExp sexp) {
        return parseSingleWrapped(sexp,
                                  "user_defined_type_name",
                                  new Function1<String, UserDefinedTypeName>() {
                                      public UserDefinedTypeName apply(final String name) {
                                          return new UserDefinedTypeName(name);
                                      }
                                  });
    }

    public static ConstructorName parseConstructorName(final SExp sexp) {
        return parseSingleWrapped(sexp,
                                  "constructor_name",
                                  new Function1<String, ConstructorName>() {
                                      public ConstructorName apply(final String name) {
                                          return new ConstructorName(name);
                                      }
                                  });
    }
    
    public static ConstructorDefinition parseCDef(final SExp sexp) {
        final CaptureSExpPattern cn = new CaptureSExpPattern();
        final CaptureSExpPattern t = new CaptureSExpPattern();
        return new ConstantListPattern(new ConstantAtomPattern("cdef"),
                                       cn,
                                       t).ifMatches(sexp,
                                                    new Function0<ConstructorDefinition>() {
                public ConstructorDefinition apply() {
                    return new ConstructorDefinition(parseConstructorName(cn.getCapture()),
                                                     parseType(t.getCapture()));
                }
            });
    }

    public static ImmutableList<ConstructorDefinition> parseCDefs(final ImmutableList<SExp> list) {
        return list.map(new Function1<SExp, ConstructorDefinition>() {
                public ConstructorDefinition apply(final SExp input) {
                    return parseCDef(input);
                }
            });
    }
    
    public static PartialFunction<Type> primitiveType(final String typeName,
                                                      final Type returnType) {
        return new PatternFunction<Type>(new ConstantAtomPattern(typeName)) {
            protected Type internal() {
                return returnType;
            }
        };
    }

    public static PartialFunction<Type> functionType() {
        final CaptureSExpPattern t1 = new CaptureSExpPattern();
        final CaptureSExpPattern t2 = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("type_function"),
                                    t1,
                                    t2);
        return new PatternFunction<Type>(pattern) {
            protected Type internal() {
                return new FunctionType(parseType(t1.getCapture()),
                                        parseType(t2.getCapture()));
            }
        };
    }

    public static PartialFunction<Type> tupleType() {
        final CaptureListPattern types = new CaptureListPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("type_tuple"),
                                    types);
        return new PatternFunction<Type>(pattern) {
            protected Type internal() {
                return new TupleType(parseTypes(types.getCapture()));
            }
        };
    }

    public static PartialFunction<Type> userType() {
        final CaptureSExpPattern un = new CaptureSExpPattern();
        final CaptureListPattern types = new CaptureListPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("type_user"),
                                    un,
                                    types);
        return new PatternFunction<Type>(pattern) {
            protected Type internal() {
                return new UserType(parseUserDefinedTypeName(un.getCapture()),
                                    parseTypes(types.getCapture()));
            }
        };
    }

    public static PartialFunction<Type> typeVariableType() {
        final CaptureSExpPattern tv = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("type_variable"),
                                    tv);
        return new PatternFunction<Type>(pattern) {
            protected Type internal() {
                return new TypeVariableType(parseTypeVariable(tv.getCapture()));
            }
        };
    }

    public static PartialFunction<Type> typePlaceholder() {
        final CaptureAtomPattern n = new CaptureAtomPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("type_placeholder"),
                                    n);
        return new PatternFunction<Type>(pattern) {
            protected Type internal() {
                return new TypePlaceholder(Integer.parseInt(n.getCapture()));
            }
        };
    }
    
    public static ImmutableList<Type> parseTypes(final ImmutableList<SExp> list) {
        return list.map(new Function1<SExp, Type>() {
                public Type apply(final SExp input) {
                    return parseType(input);
                }
            });
    }

    @SuppressWarnings("unchecked")
    public static PartialFunction<Type> typeParser() {
        return PartialFunction.or(primitiveType("type_string", new StringType()),
                                  primitiveType("type_boolean", new BooleanType()),
                                  primitiveType("type_integer", new IntegerType()),
                                  primitiveType("type_unit", new UnitType()),
                                  functionType(),
                                  tupleType(),
                                  userType(),
                                  typeVariableType(),
                                  typePlaceholder());
    }
    
    public static Type parseType(final SExp sexp) {
        return typeParser().apply(sexp);
    }

    public static PartialFunction<Exp> expVariable() {
        final CaptureSExpPattern x = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_variable"),
                                    x);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new VariableExp(parseVariable(x.getCapture()));
            }
        };
    }

    public static PartialFunction<Exp> expString() {
        final CaptureAtomPattern string = new CaptureAtomPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_string"),
                                    string);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new StringExp(string.getCapture());
            }
        };
    }

    public static boolean toBoolean(final String input) {
        if (input.equals("true")) {
            return true;
        } else if (input.equals("false")) {
            return false;
        } else {
            throw new SimpleScalaParseException("Not a boolean: " + input);
        }
    }
    
    public static PartialFunction<Exp> expBoolean() {
        final CaptureAtomPattern b = new CaptureAtomPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_boolean"),
                                    b);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new BooleanExp(toBoolean(b.getCapture()));
            }
        };
    }

    public static PartialFunction<Exp> expInt() {
        final CaptureAtomPattern i = new CaptureAtomPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_int"),
                                    i);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new IntExp(Integer.parseInt(i.getCapture()));
            }
        };
    }

    public static PartialFunction<Exp> expUnit() {
        return new PatternFunction<Exp>(new ConstantAtomPattern("exp_unit")) {
            protected Exp internal() {
                return new UnitExp();
            }
        };
    }

    public static PartialFunction<Binop> bop(final String name,
                                             final Binop binop) {
        return new PatternFunction<Binop>(new ConstantAtomPattern(name)) {
            protected Binop internal() {
                return binop;
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    public static PartialFunction<Binop> binopParser() {
        return PartialFunction.or(bop("+", Binop.PLUS),
                                  bop("-", Binop.MINUS),
                                  bop("*", Binop.TIMES),
                                  bop("/", Binop.DIV),
                                  bop("&&", Binop.AND),
                                  bop("||", Binop.OR),
                                  bop("<", Binop.LT),
                                  bop("<=", Binop.LTE),
                                  bop("++", Binop.CONCAT));
    }
    
    public static Binop parseBinop(final SExp sexp) {
        return binopParser().apply(sexp);
    }

    public static PartialFunction<Exp> expBinop() {
        final CaptureSExpPattern e1 = new CaptureSExpPattern();
        final CaptureSExpPattern binop = new CaptureSExpPattern();
        final CaptureSExpPattern e2 = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_binop"),
                                    e1,
                                    binop,
                                    e2);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new BinopExp(parseExp(e1.getCapture()),
                                    parseBinop(binop.getCapture()),
                                    parseExp(e2.getCapture()));
            }
        };
    }

    public static PartialFunction<Exp> expFunction() {
        final CaptureSExpPattern x = new CaptureSExpPattern();
        final CaptureSExpPattern e = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_function"),
                                    x,
                                    e);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new FunctionExp(parseVariable(x.getCapture()),
                                       parseExp(e.getCapture()));
            }
        };
    }

    public static PartialFunction<Exp> expAnonCall() {
        final CaptureSExpPattern e1 = new CaptureSExpPattern();
        final CaptureSExpPattern e2 = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_anoncall"),
                                    e1,
                                    e2);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new AnonCallExp(parseExp(e1.getCapture()),
                                       parseExp(e2.getCapture()));
            }
        };
    }

    public static PartialFunction<Exp> expNamedCall() {
        final CaptureSExpPattern fn = new CaptureSExpPattern();
        final CaptureSExpPattern e = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_namedcall"),
                                    fn,
                                    e);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new NamedCallExp(parseFunctionName(fn.getCapture()),
                                        parseExp(e.getCapture()));
            }
        };
    }

    public static PartialFunction<Exp> expIf() {
        final CaptureSExpPattern e1 = new CaptureSExpPattern();
        final CaptureSExpPattern e2 = new CaptureSExpPattern();
        final CaptureSExpPattern e3 = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_if"),
                                    e1,
                                    e2,
                                    e3);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new IfExp(parseExp(e1.getCapture()),
                                 parseExp(e2.getCapture()),
                                 parseExp(e3.getCapture()));
            }
        };
    }

    public static Val parseVal(final SExp sexp) {
        final CaptureSExpPattern x = new CaptureSExpPattern();
        final CaptureSExpPattern e = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("val"),
                                    x,
                                    e);
        return pattern.ifMatches(sexp,
                                 new Function0<Val>() {
                                     public Val apply() {
                                         return new Val(parseVariable(x.getCapture()),
                                                        parseExp(e.getCapture()));
                                     }
                                 });
    }

    public static ImmutableList<Val> parseVals(final ImmutableList<SExp> sexps) {
        return sexps.map(new Function1<SExp, Val>() {
                public Val apply(final SExp input) {
                    return parseVal(input);
                }
            });
    }
                                                                      
    public static PartialFunction<Exp> expBlock() {
        final CaptureListPattern vals = new CaptureListPattern();
        final CaptureSExpPattern e = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_block"),
                                    vals,
                                    e);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new BlockExp(parseVals(vals.getCapture()),
                                    parseExp(e.getCapture()));
            }
        };
    }

    public static PartialFunction<Exp> expTuple() {
        final CaptureListPattern es = new CaptureListPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_tuple"),
                                    es);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new TupleExp(parseExps(es.getCapture()));
            }
        };
    }

    public static ImmutableList<Exp> parseExps(final ImmutableList<SExp> list) {
        return list.map(new Function1<SExp, Exp>() {
                public Exp apply(final SExp input) {
                    return parseExp(input);
                }
            });
    }

    public static PartialFunction<Exp> expConstructor() {
        final CaptureSExpPattern cn = new CaptureSExpPattern();
        final CaptureSExpPattern e = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_constructor"),
                                    cn,
                                    e);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new ConstructorExp(parseConstructorName(cn.getCapture()),
                                          parseExp(e.getCapture()));
            }
        };
    }

    public static PartialFunction<Case> caseConstructor() {
        final CaptureSExpPattern cn = new CaptureSExpPattern();
        final CaptureSExpPattern x = new CaptureSExpPattern();
        final CaptureSExpPattern e = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("case_constructor"),
                                    cn,
                                    x,
                                    e);
        return new PatternFunction<Case>(pattern) {
            protected Case internal() {
                return new ConstructorCase(parseConstructorName(cn.getCapture()),
                                           parseVariable(x.getCapture()),
                                           parseExp(e.getCapture()));
            }
        };
    }
    
    public static PartialFunction<Case> caseTup() {
        final CaptureListPattern xs = new CaptureListPattern();
        final CaptureSExpPattern e = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("case_tup"),
                                    xs,
                                    e);
        return new PatternFunction<Case>(pattern) {
            protected Case internal() {
                return new TupCase(parseVariables(xs.getCapture()),
                                   parseExp(e.getCapture()));
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static PartialFunction<Case> caseParser() {
        return PartialFunction.or(caseConstructor(),
                                  caseTup());
    }
    
    public static Case parseCase(final SExp sexp) {
        return caseParser().apply(sexp);
    }
    
    public static ImmutableList<Case> parseCases(final ImmutableList<SExp> sexps) {
        return sexps.map(new Function1<SExp, Case>() {
                public Case apply(final SExp input) {
                    return parseCase(input);
                }
            });
    }
    
    public static PartialFunction<Exp> expMatch() {
        final CaptureSExpPattern e = new CaptureSExpPattern();
        final CaptureListPattern cases = new CaptureListPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("exp_match"),
                                    e,
                                    cases);
        return new PatternFunction<Exp>(pattern) {
            protected Exp internal() {
                return new MatchExp(parseExp(e.getCapture()),
                                    parseCases(cases.getCapture()));
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    public static PartialFunction<Exp> expParser() {
        return PartialFunction.or(expVariable(),
                                  expString(),
                                  expBoolean(),
                                  expInt(),
                                  expUnit(),
                                  expBinop(),
                                  expFunction(),
                                  expAnonCall(),
                                  expNamedCall(),
                                  expIf(),
                                  expBlock(),
                                  expTuple(),
                                  expConstructor(),
                                  expMatch());
    }
    
    public static Exp parseExp(final SExp sexp) {
        return expParser().apply(sexp);
    }

    public static UserDefinedTypeDef parseTDef(final SExp sexp) {
        final CaptureSExpPattern tn = new CaptureSExpPattern();
        final CaptureListPattern tvs = new CaptureListPattern();
        final CaptureListPattern cdefs = new CaptureListPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("tdef"),
                                    tn,
                                    tvs,
                                    cdefs);
        return new PatternFunction<UserDefinedTypeDef>(pattern) {
            protected UserDefinedTypeDef internal() {
                return new UserDefinedTypeDef(parseUserDefinedTypeName(tn.getCapture()),
                                              parseTypeVariables(tvs.getCapture()),
                                              parseCDefs(cdefs.getCapture()));
            }
        }.apply(sexp);
    }

    public static ImmutableList<UserDefinedTypeDef> parseTDefs(final ImmutableList<SExp> list) {
        return list.map(new Function1<SExp, UserDefinedTypeDef>() {
                public UserDefinedTypeDef apply(final SExp input) {
                    return parseTDef(input);
                }
            });
    }

    public static Def parseFDef(final SExp sexp) {
        final CaptureSExpPattern fn = new CaptureSExpPattern();
        final CaptureListPattern tvs = new CaptureListPattern();
        final CaptureSExpPattern x = new CaptureSExpPattern();
        final CaptureSExpPattern t1 = new CaptureSExpPattern();
        final CaptureSExpPattern t2 = new CaptureSExpPattern();
        final CaptureSExpPattern e = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("fdef"),
                                    fn,
                                    tvs,
                                    x,
                                    t1,
                                    t2,
                                    e);
        return new PatternFunction<Def>(pattern) {
            protected Def internal() {
                return new Def(parseFunctionName(fn.getCapture()),
                               parseTypeVariables(tvs.getCapture()),
                               parseVariable(x.getCapture()),
                               parseType(t1.getCapture()),
                               parseType(t2.getCapture()),
                               parseExp(e.getCapture()));
            }
        }.apply(sexp);
    }

    public static ImmutableList<Def> parseFDefs(final ImmutableList<SExp> sexps) {
        return sexps.map(new Function1<SExp, Def>() {
                public Def apply(final SExp input) {
                    return parseFDef(input);
                }
            });
    }
    
    public static Program parseProgram(final SExp sexp) {
        final CaptureListPattern tdefs = new CaptureListPattern();
        final CaptureListPattern fdefs = new CaptureListPattern();
        final CaptureSExpPattern exp = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("program"),
                                    tdefs,
                                    fdefs,
                                    exp);
        return new PatternFunction<Program>(pattern) {
            protected Program internal() {
                return new Program(parseTDefs(tdefs.getCapture()),
                                   parseFDefs(fdefs.getCapture()),
                                   parseExp(exp.getCapture()));
            }
        }.apply(sexp);
    }

    public static PartialFunction<Option<Type>> noneTypeParser() {
        return new PatternFunction<Option<Type>>(new ConstantAtomPattern("ILLTYPED")) {
            protected Option<Type> internal() {
                return new None<Type>();
            }
        };
    }

    public static PartialFunction<Option<Type>> someTypeParser() {
        final CaptureSExpPattern other = new CaptureSExpPattern();
        return new PatternFunction<Option<Type>>(other) {
            protected Option<Type> internal() {
                return new Some<Type>(parseType(other.getCapture()));
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static PartialFunction<Option<Type>> possibleTypeParser() {
        return PartialFunction.or(noneTypeParser(),
                                  someTypeParser());
    }
    
    public static Option<Type> parsePossibleType(final SExp sexp) {
        return possibleTypeParser().apply(sexp);
    }

    public static FullTest parseFullTest(final String testName,
                                         final SExp sexp) {
        final CaptureSExpPattern p = new CaptureSExpPattern();
        final CaptureSExpPattern t = new CaptureSExpPattern();
        final SExpPattern pattern =
            new ConstantListPattern(new ConstantAtomPattern("test"),
                                    p,
                                    t);
        return new PatternFunction<FullTest>(pattern) {
            protected FullTest internal() {
                return new FullTest(testName,
                                    parseProgram(p.getCapture()),
                                    parsePossibleType(t.getCapture()));
            }
        }.apply(sexp);
    }

    public static FullTest parseTestFromString(final String testName,
                                               final String testContents) {
        final ImmutableList<SExp> sexps = SExpParser.parse(testContents);
        if (sexps.size() != 1) {
            throw new SimpleScalaParseException("more than one toplevel sexp");
        }
        return parseFullTest(testName, sexps.first());
    }
} // SimpleScalaSExpParser
