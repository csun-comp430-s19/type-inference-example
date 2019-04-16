package simplescala_experimentation.syntax;

public interface CaseVisitor<D, U, E extends Exception> {
    public U visit(ConstructorCase c, D down) throws E;
    public U visit(TupCase t, D down) throws E;
}
