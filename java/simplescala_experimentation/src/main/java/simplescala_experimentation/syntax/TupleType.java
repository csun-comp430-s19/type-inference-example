package simplescala_experimentation.syntax;

import simplescala_experimentation.util.ImmutableList;

public class TupleType implements Type {
    public final ImmutableList<Type> types;

    public TupleType(final ImmutableList<Type> types) {
        this.types = types;
    }
    
    public <D, U, E extends Exception> U visitType(final TypeVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return (other instanceof TupleType &&
                ((TupleType)other).types.equals(types));
    }

    public String toString() {
        return "(" + types.mkString(", ") + ")";
    }
}
