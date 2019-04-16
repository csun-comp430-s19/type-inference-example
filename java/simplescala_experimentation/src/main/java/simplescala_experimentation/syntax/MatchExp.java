package simplescala_experimentation.syntax;

import simplescala_experimentation.util.ImmutableList;

public class MatchExp implements Exp {
    public final Exp e;
    public final ImmutableList<Case> cases;

    public MatchExp(final Exp e, final ImmutableList<Case> cases) {
        this.e = e;
        this.cases = cases;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof MatchExp) {
            final MatchExp me = (MatchExp)other;
            return me.e.equals(e) && me.cases.equals(cases);
        } else {
            return false;
        }
    }
}
