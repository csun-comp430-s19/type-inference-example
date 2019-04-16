package simplescala_experimentation.syntax;

public class FunctionExp implements Exp {
    public final Variable x;
    public final Exp e;

    public FunctionExp(final Variable x, final Exp e) {
        this.x = x;
        this.e = e;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof FunctionExp) {
            final FunctionExp fe = (FunctionExp)other;
            return fe.x.equals(x) && fe.e.equals(e);
        } else {
            return false;
        }
    }
}
