package simplescala_experimentation.typechecker;

import simplescala_experimentation.util.ImmutableList;
import simplescala_experimentation.util.ImmutableMap;
import simplescala_experimentation.syntax.TypeVariable;
import simplescala_experimentation.syntax.ConstructorName;
import simplescala_experimentation.syntax.Type;

public class TypeDef {
    public final ImmutableList<TypeVariable> tvs;
    public final ImmutableMap<ConstructorName, Type> cdefs;

    public TypeDef(final ImmutableList<TypeVariable> tvs,
                   final ImmutableMap<ConstructorName, Type> cdefs) {
        this.tvs = tvs;
        this.cdefs = cdefs;
    }

    public boolean equals(final Object other) {
        if (other instanceof TypeDef) {
            final TypeDef td = (TypeDef)other;
            return td.tvs.equals(tvs) && td.cdefs.equals(cdefs);
        } else {
            return false;
        }
    }
}
