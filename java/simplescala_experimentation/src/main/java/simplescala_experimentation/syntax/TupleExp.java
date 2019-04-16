package simplescala_experimentation.syntax;

import simplescala_experimentation.util.ImmutableList;

public class TupleExp implements Exp {
    public final ImmutableList<Exp> es;

    public TupleExp(final ImmutableList<Exp> es) {
        this.es = es;
    }
    
    public <D, U, E extends Exception> U visitExp(ExpVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return (other instanceof TupleExp &&
                ((TupleExp)other).es.equals(es));
    }
}
