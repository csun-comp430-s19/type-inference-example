package simplescala_experimentation.syntax;

public interface ExpVisitor<D, U, E extends Exception> {
    public U visit(VariableExp v, D down) throws E;
    public U visit(StringExp s, D down) throws E;
    public U visit(BooleanExp b, D down) throws E;
    public U visit(IntExp i, D down) throws E;
    public U visit(UnitExp u, D down) throws E;
    public U visit(BinopExp b, D down) throws E;
    public U visit(FunctionExp f, D down) throws E;
    public U visit(AnonCallExp a, D down) throws E;
    public U visit(NamedCallExp n, D down) throws E;
    public U visit(IfExp i, D down) throws E;
    public U visit(BlockExp b, D down) throws E;
    public U visit(TupleExp t, D down) throws E;
    public U visit(ConstructorExp c, D down) throws E;
    public U visit(MatchExp m, D down) throws E;
}
