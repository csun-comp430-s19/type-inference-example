package simplescala_experimentation.syntax;

public interface Type {
    public <D, U, E extends Exception> U visitType(TypeVisitor<D, U, E> typeVisitor, D down) throws E;
}
