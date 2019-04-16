package simplescala_experimentation.syntax;

public class IfExp implements Exp {
    public final Exp e1;
    public final Exp e2;
    public final Exp e3;

    public IfExp(final Exp e1, final Exp e2, final Exp e3) {
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof IfExp) {
            final IfExp ie = (IfExp)other;
            return (ie.e1.equals(e1) &&
                    ie.e2.equals(e2) &&
                    ie.e3.equals(e3));
        } else {
            return false;
        }
    }
}
