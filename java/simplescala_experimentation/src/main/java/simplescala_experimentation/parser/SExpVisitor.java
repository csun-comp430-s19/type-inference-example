package simplescala_experimentation.parser;

import simplescala_experimentation.util.ImmutableList;

public interface SExpVisitor<D, U> {
    public U visit(String atom, D down);
    public U visit(ImmutableList<SExp> list, D down);
}
