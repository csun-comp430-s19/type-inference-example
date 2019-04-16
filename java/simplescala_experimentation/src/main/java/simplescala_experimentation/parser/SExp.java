package simplescala_experimentation.parser;

import simplescala_experimentation.util.JavaUnit;

public abstract class SExp {
    public <U> U visitSExp(final SExpVisitor<JavaUnit, U> visitor) {
        return visitSExp(visitor, JavaUnit.UNIT);
    }

    // ---BEGIN ABSTRACT MEMBERS---
    public abstract <D, U> U visitSExp(SExpVisitor<D, U> visitor, D down);
    // ---END ABSTRACT MEMBERS---
}
