package simplescala_experimentation.syntax;

public class UnitType implements Type {
    public <D, U, E extends Exception> U visitType(final TypeVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return other instanceof UnitType;
    }

    public String toString() {
        return "Unit";
    }
}
