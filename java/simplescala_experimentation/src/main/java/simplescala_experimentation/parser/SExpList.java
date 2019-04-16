package simplescala_experimentation.parser;

import simplescala_experimentation.util.ImmutableList;

public class SExpList extends SExp {
    public final ImmutableList<SExp> list;
    public SExpList(final ImmutableList<SExp> list) {
        this.list = list;
    }

    public <D, U> U visitSExp(final SExpVisitor<D, U> visitor, final D down) {
        return visitor.visit(list, down);
    }

    public boolean equals(final Object other) {
        return (other instanceof SExpList &&
                ((SExpList)other).list.equals(list));
    }

    public String toString() {
        return "SExpList(" + list.mkString(", ") + ")";
    }
}
