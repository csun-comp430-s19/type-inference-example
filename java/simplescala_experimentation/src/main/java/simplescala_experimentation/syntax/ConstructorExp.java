package simplescala_experimentation.syntax;

public class ConstructorExp implements Exp {
    public final ConstructorName cn;
    public final Exp e;

    public ConstructorExp(final ConstructorName cn, final Exp e) {
        this.cn = cn;
        this.e = e;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof ConstructorExp) {
            final ConstructorExp ce = (ConstructorExp)other;
            return ce.cn.equals(cn) && ce.e.equals(e);
        } else {
            return false;
        }
    }
}
