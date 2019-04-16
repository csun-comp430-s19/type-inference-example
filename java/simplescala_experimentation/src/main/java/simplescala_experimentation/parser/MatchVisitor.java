package simplescala_experimentation.parser;

import simplescala_experimentation.util.JavaUnit;
import simplescala_experimentation.util.ImmutableList;

public class MatchVisitor implements SExpVisitor<JavaUnit, Boolean> {
    public boolean visit(final String atom) {
        return false;
    }

    public boolean visit(final ImmutableList<SExp> list) {
        return false;
    }

    public final Boolean visit(final String atom, final JavaUnit down) {
        return new Boolean(visit(atom));
    }

    public final Boolean visit(final ImmutableList<SExp> list, final JavaUnit down) {
        return new Boolean(visit(list));
    }
}
