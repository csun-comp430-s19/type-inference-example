package simplescala_experimentation.syntax;

public interface Exp {
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E;
}
