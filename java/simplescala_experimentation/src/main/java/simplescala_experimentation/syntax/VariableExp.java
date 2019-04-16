package simplescala_experimentation.syntax;

public class VariableExp implements Exp {
    public final Variable x;

    public VariableExp(final Variable x) {
        this.x = x;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return (other instanceof VariableExp &&
                ((VariableExp)other).x.equals(x));
    }
}
