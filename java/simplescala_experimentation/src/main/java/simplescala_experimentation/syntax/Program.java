package simplescala_experimentation.syntax;

import simplescala_experimentation.util.ImmutableList;

public class Program {
    public final ImmutableList<UserDefinedTypeDef> tdefs;
    public final ImmutableList<Def> defs;
    public final Exp e;

    public Program(final ImmutableList<UserDefinedTypeDef> tdefs,
                   final ImmutableList<Def> defs,
                   final Exp e) {
        this.tdefs = tdefs;
        this.defs = defs;
        this.e = e;
    }

    public boolean equals(final Object other) {
        if (other instanceof Program) {
            final Program p = (Program)other;
            return (p.tdefs.equals(tdefs) &&
                    p.defs.equals(defs) &&
                    p.e.equals(e));
        } else {
            return false;
        }
    }
}
