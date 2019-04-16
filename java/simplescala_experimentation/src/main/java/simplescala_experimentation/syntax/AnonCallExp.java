package simplescala_experimentation.syntax;

public class AnonCallExp implements Exp {
    public final Exp e1;
    public final Exp e2;

    public AnonCallExp(final Exp e1, final Exp e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof AnonCallExp) {
            final AnonCallExp ae = (AnonCallExp)other;
            return ae.e1.equals(e1) && ae.e2.equals(e2);
        } else {
            return false;
        }
    }
}
