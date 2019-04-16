package simplescala_experimentation.syntax;

import simplescala_experimentation.util.ImmutableList;

public class BlockExp implements Exp {
    public final ImmutableList<Val> vals;
    public final Exp e;

    public BlockExp(final ImmutableList<Val> vals,
                    final Exp e) {
        this.vals = vals;
        this.e = e;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof BlockExp) {
            final BlockExp be = (BlockExp)other;
            return be.vals.equals(vals) && be.e.equals(e);
        } else {
            return false;
        }
    }
}
