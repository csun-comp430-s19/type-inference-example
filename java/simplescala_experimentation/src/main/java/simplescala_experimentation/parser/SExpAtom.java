package simplescala_experimentation.parser;

public class SExpAtom extends SExp {
    public final String atom;

    public SExpAtom(final String atom) {
        this.atom = atom;
    }

    public <D, U> U visitSExp(final SExpVisitor<D, U> visitor, final D down) {
        return visitor.visit(atom, down);
    }

    public boolean equals(final Object other) {
        return (other instanceof SExpAtom &&
                ((SExpAtom)other).atom.equals(atom));
    }

    public String toString() {
        return "SExpAtom(" + atom + ")";
    }
}
