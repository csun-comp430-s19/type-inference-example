package simplescala_experimentation.syntax;

public interface TypeVisitor<D, U, E extends Exception> {
    public U visit(StringType st, D down) throws E;
    public U visit(BooleanType bt, D down) throws E;
    public U visit(IntegerType it, D down) throws E;
    public U visit(UnitType ut, D down) throws E;
    public U visit(FunctionType ft, D down) throws E;
    public U visit(TupleType tt, D down) throws E;
    public U visit(UserType ut, D down) throws E;
    public U visit(TypeVariableType tv, D down) throws E;
    public U visit(TypePlaceholder tp, D down) throws E;
}


    
