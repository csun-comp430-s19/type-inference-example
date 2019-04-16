package simplescala_experimentation.typechecker;

import simplescala_experimentation.util.ImmutableList;
import simplescala_experimentation.syntax.Type;
import simplescala_experimentation.syntax.TypeVariable;

public class NamedFunctionDef {
    public final ImmutableList<TypeVariable> tvs;
    public final Type paramType;
    public final Type returnType;

    public NamedFunctionDef(final ImmutableList<TypeVariable> tvs,
                            final Type paramType,
                            final Type returnType) {
        this.tvs = tvs;
        this.paramType = paramType;
        this.returnType = returnType;
    }

    public boolean equals(final Object other) {
        if (other instanceof NamedFunctionDef) {
            final NamedFunctionDef d = (NamedFunctionDef)other;
            return (d.tvs.equals(tvs) &&
                    d.paramType.equals(paramType) &&
                    d.returnType.equals(returnType));
        } else {
            return false;
        }
    }
}
