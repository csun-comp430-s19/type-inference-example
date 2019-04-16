package simplescala_experimentation.typechecker;

import simplescala_experimentation.util.*;
import simplescala_experimentation.syntax.*;


public class ConstraintStore {
    public final ImmutableMap<TypePlaceholder, Type> map;
    private final LookupVisitor lookupVisitor;
    
    public ConstraintStore(final ImmutableMap<TypePlaceholder, Type> map) {
        this.map = map;
        lookupVisitor = new LookupVisitor();
    }

    public ConstraintStore() {
        this(new ImmutableMap<TypePlaceholder, Type>());
    }

    private class LookupVisitor extends TypeVisitorWithDefault<JavaUnit, Type, IllTyped> {
        public Type visit(final TypePlaceholder tp, JavaUnit down) {
            return lookup(tp);
        }

        public Type doDefault(final Type t, JavaUnit down) {
            return t;
        }
    }
    
    public Type lookup(final TypePlaceholder p) throws IllTyped {
        final Option<Type> op = map.get(p);
        if (op.isDefined()) {
            return op.get().visitType(lookupVisitor, JavaUnit.UNIT);
        } else {
            return p;
        }
    }

    public Type lookupTypeSingleLevel(final Type typ) throws IllTyped {
        return typ.visitType(lookupVisitor, JavaUnit.UNIT);
    }

    public ImmutableList<Type> lookupTypesFull(final ImmutableList<Type> types) throws IllTyped {
        return types.map(new Function1<Type, Type>() {
                public Type apply(final Type input) {
                    return lookupTypeFull(input);
                }
            });
    }

    public class LookupTypeFullVisitor implements TypeVisitor<JavaUnit, Type, IllTyped> {
        public Type visit(final StringType t, final JavaUnit down) throws IllTyped { return t; }
        public Type visit(final BooleanType t, final JavaUnit down) throws IllTyped { return t; }
        public Type visit(final IntegerType t, final JavaUnit down) throws IllTyped { return t; }
        public Type visit(final UnitType t, final JavaUnit down) throws IllTyped { return t; }
        public Type visit(final TypeVariableType t, final JavaUnit down) throws IllTyped { return t; }
        public Type visit(final FunctionType t, final JavaUnit down) throws IllTyped {
            return new FunctionType(t.paramType.visitType(this, down),
                                    t.returnType.visitType(this, down));
        }
        public Type visit(final TupleType t, final JavaUnit down) throws IllTyped {
            return new TupleType(lookupTypesFull(t.types));
        }
        public Type visit(final UserType t, final JavaUnit down) throws IllTyped {
            return new UserType(t.un, lookupTypesFull(t.types));
        }
        public Type visit(final TypePlaceholder t, final JavaUnit down) throws IllTyped {
            if (map.contains(t)) {
                return lookup(t).visitType(this, down);
            } else {
                return t;
            }
        }
    }
                
    public Type lookupTypeFull(final Type t) throws IllTyped {
        return t.visitType(new LookupTypeFullVisitor(), JavaUnit.UNIT);
    }
    
    private boolean typesContains(final ImmutableList<Type> types, final TypePlaceholder p) throws IllTyped {
        return types.exists(new Predicate<Type>() {
                public boolean matches(final Type type) {
                    return typeContains(type, p);
                }
            });
    }

    private class TypeContainsPlaceholderVisitor extends TypeVisitorWithDefault<JavaUnit, Boolean, IllTyped> {
        public final TypePlaceholder p;
        public TypeContainsPlaceholderVisitor(final TypePlaceholder p) {
            this.p = p;
        }
        public Boolean visit(final TypePlaceholder tp, final JavaUnit down) throws IllTyped {
            return new Boolean(p.equals(tp));
        }
        public Boolean doDefault(final Type t, JavaUnit down) throws IllTyped {
            return new Boolean(typeContains(t, p));
        }
    }
    
    private class TypeContainsVisitor implements TypeVisitor<JavaUnit, Boolean, IllTyped> {
        public final TypePlaceholder p;
        public TypeContainsVisitor(final TypePlaceholder p) {
            this.p = p;
        }
        public Boolean visit(final StringType s, final JavaUnit down) throws IllTyped { return false; }
        public Boolean visit(final BooleanType b, final JavaUnit down) throws IllTyped { return false; }
        public Boolean visit(final IntegerType b, final JavaUnit down) throws IllTyped { return false; }
        public Boolean visit(final UnitType b, final JavaUnit down) throws IllTyped { return false; }
        public Boolean visit(final TypeVariableType b, final JavaUnit down) throws IllTyped { return false; }
        public Boolean visit(final FunctionType ft, final JavaUnit down) throws IllTyped {
            return new Boolean(typeContains(ft.paramType, p) ||
                               typeContains(ft.returnType, p));
        }
        public Boolean visit(final TupleType tt, final JavaUnit down) throws IllTyped {
            return new Boolean(typesContains(tt.types, p));
        }
        public Boolean visit(final UserType ut, final JavaUnit down) throws IllTyped {
            return new Boolean(typesContains(ut.types, p));
        }
        public Boolean visit(final TypePlaceholder tp, final JavaUnit down) throws IllTyped {
            return lookup(tp).visitType(new TypeContainsPlaceholderVisitor(p), down);
        }
    }
    
    private boolean typeContains(final Type typ, final TypePlaceholder p) throws IllTyped {
        return typ.visitType(new TypeContainsVisitor(p), JavaUnit.UNIT).booleanValue();
    }

    private ConstraintStore unifyPlaceholderType(final TypePlaceholder p, final Type typ)
        throws IllTyped {
        if (p.equals(typ)) {
            return this;
        } else if (!typeContains(typ, p)) {
            return new ConstraintStore(map.add(p, typ));
        } else {
            throw new IllTyped();
        }
    }

    public ConstraintStore unify(final ImmutableList<Type> t1s,
                                 final ImmutableList<Type> t2s)
        throws IllTyped {
        if (t1s.size() != t2s.size()) {
            throw new IllTyped("Type list different sizes:\n" +
                               t1s.toString() + "\n" +
                               t2s.toString());
        } else {
            ConstraintStore store = this;
            for (final Pair<Type, Type> pair : t1s.zip(t2s)) {
                store = store.unify(pair.first, pair.second);
            }
            return store;
        }
    }

    // This cannot use the visitor pattern since we need to look
    // at both types at the same time, and deconstruct them
    // piecewise
    public ConstraintStore unify(final Type t1, final Type t2) throws IllTyped {
        final Type lookT1 = lookupTypeSingleLevel(t1);
        final Type lookT2 = lookupTypeSingleLevel(t2);

        if (lookT1 instanceof TypePlaceholder) {
            return unifyPlaceholderType((TypePlaceholder)lookT1, lookT2);
        } else if (lookT2 instanceof TypePlaceholder) {
            return unifyPlaceholderType((TypePlaceholder)lookT2, lookT1);
        } else if ((lookT1 instanceof StringType && lookT2 instanceof StringType) ||
                   (lookT1 instanceof BooleanType && lookT2 instanceof BooleanType) ||
                   (lookT1 instanceof IntegerType && lookT2 instanceof IntegerType) ||
                   (lookT1 instanceof UnitType && lookT2 instanceof UnitType)) {
            return this;
        } else if (lookT1 instanceof TypeVariableType && lookT2 instanceof TypeVariableType) {
            if (lookT1.equals(lookT2)) {
                return this;
            } else {
                throw new IllTyped();
            }
        } else if (lookT1 instanceof FunctionType && lookT2 instanceof FunctionType) {
            final FunctionType f1 = (FunctionType)lookT1;
            final FunctionType f2 = (FunctionType)lookT2;
            return unify(f1.paramType, f2.paramType).unify(f1.returnType, f2.returnType);
        } else if (lookT1 instanceof TupleType && lookT2 instanceof TupleType) {
            return unify(((TupleType)lookT1).types, ((TupleType)lookT2).types);
        } else if (lookT1 instanceof UserType && lookT2 instanceof UserType) {
            final UserType u1 = (UserType)lookT1;
            final UserType u2 = (UserType)lookT2;
            if (u1.un.equals(u2.un)) {
                return unify(u1.types, u2.types);
            } else {
                throw new IllTyped();
            }
        } else {
            throw new IllTyped();
        }
    } // unify
} // ConstraintStore
