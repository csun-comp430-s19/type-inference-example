package simplescala_experimentation.syntax;

public class StringExp implements Exp {
    public final String str;

    public StringExp(final String str) {
        this.str = str;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return (other instanceof StringExp &&
                ((StringExp)other).str.equals(str));
    }
}
