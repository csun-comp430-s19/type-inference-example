package simplescala_experimentation.syntax;

public class NamedCallExp implements Exp {
    public final FunctionName fn;
    public final Exp e;

    public NamedCallExp(final FunctionName fn, final Exp e) {
        this.fn = fn;
        this.e = e;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof NamedCallExp) {
            final NamedCallExp ne = (NamedCallExp)other;
            return ne.fn.equals(fn) && ne.e.equals(e);
        } else {
            return false;
        }
    }
}
