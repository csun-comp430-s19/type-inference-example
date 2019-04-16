package simplescala_experimentation.syntax;

public class IntegerType implements Type {
    public <D, U, E extends Exception> U visitType(final TypeVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return other instanceof IntegerType;
    }

    public String toString() {
        return "Int";
    }
}
