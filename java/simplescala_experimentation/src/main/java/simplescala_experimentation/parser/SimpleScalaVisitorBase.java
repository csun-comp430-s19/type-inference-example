package simplescala_experimentation.parser;

import simplescala_experimentation.util.JavaUnit;
import simplescala_experimentation.util.ImmutableList;

public class SimpleScalaVisitorBase<U> implements SExpVisitor<JavaUnit, U> {
    public U visit(final String atom) {
        throw new SimpleScalaParseException();
    }
    
    public U visit(final ImmutableList<SExp> list) {
        throw new SimpleScalaParseException();
    }

    public final U visit(final String atom, final JavaUnit down) {
        return visit(atom);
    }

    public final U visit(final ImmutableList<SExp> list, final JavaUnit down) {
        return visit(list);
    }
}
