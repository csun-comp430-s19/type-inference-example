package simplescala_experimentation.syntax;

public class BinopExp implements Exp {
    public final Exp e1;
    public final Binop op;
    public final Exp e2;

    public BinopExp(final Exp e1,
                    final Binop op,
                    final Exp e2) {
        this.e1 = e1;
        this.op = op;
        this.e2 = e2;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof BinopExp) {
            final BinopExp be = (BinopExp)other;
            return (be.e1.equals(e1) &&
                    be.op == op &&
                    be.e2.equals(e2));
        } else {
            return false;
        }
    }
}
