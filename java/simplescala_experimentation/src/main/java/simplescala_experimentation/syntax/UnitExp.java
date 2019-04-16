package simplescala_experimentation.syntax;

public class UnitExp implements Exp {
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return other instanceof UnitExp;
    }
}
