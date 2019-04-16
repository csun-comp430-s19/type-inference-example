package simplescala_experimentation.syntax;

public abstract class TypeVisitorWithDefault<D, U, E extends Exception> implements TypeVisitor<D, U, E> {

    public U visit(StringType st, D down) throws E {
        return doDefault(st, down);
    }
    
    public U visit(BooleanType bt, D down) throws E {
        return doDefault(bt, down);
    }
    
    public U visit(IntegerType it, D down) throws E {
        return doDefault(it, down);
    }
    
    public U visit(UnitType ut, D down) throws E {
        return doDefault(ut, down);
    }
    
    public U visit(FunctionType ft, D down) throws E {
        return doDefault(ft, down);
    }
    
    public U visit(TupleType tt, D down) throws E {
        return doDefault(tt, down);
    }
    
    public U visit(UserType ut, D down) throws E {
        return doDefault(ut, down);
    }
    
    public U visit(TypeVariableType tv, D down) throws E {
        return doDefault(tv, down);
    }
    
    public U visit(TypePlaceholder tp, D down) throws E {
        return doDefault(tp, down);
    }

    public abstract U doDefault(Type t, D down) throws E;
}
