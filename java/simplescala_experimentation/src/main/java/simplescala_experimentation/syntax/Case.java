package simplescala_experimentation.syntax;

public interface Case {
    public <D, U, E extends Exception> U visitCase(CaseVisitor<D, U, E> visitor, D down) throws E;
}
