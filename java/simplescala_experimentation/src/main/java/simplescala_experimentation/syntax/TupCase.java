package simplescala_experimentation.syntax;

import simplescala_experimentation.util.ImmutableList;

public class TupCase implements Case {
    public final ImmutableList<Variable> xs;
    public final Exp e;

    public TupCase(final ImmutableList<Variable> xs,
                   final Exp e) {
        this.xs = xs;
        this.e = e;
    }

    public <D, U, E extends Exception> U visitCase(final CaseVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof TupCase) {
            final TupCase tc = (TupCase)other;
            return tc.xs.equals(xs) && tc.e.equals(e);
        } else {
            return false;
        }
    }
}
