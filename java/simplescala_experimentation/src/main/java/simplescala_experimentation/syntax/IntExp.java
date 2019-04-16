package simplescala_experimentation.syntax;

public class IntExp implements Exp {
    public final int i;

    public IntExp(final int i) {
        this.i = i;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return (other instanceof IntExp &&
                ((IntExp)other).i == i);
    }
}
