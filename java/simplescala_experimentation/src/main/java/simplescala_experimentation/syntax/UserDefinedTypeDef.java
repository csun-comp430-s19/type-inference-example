package simplescala_experimentation.syntax;

import simplescala_experimentation.util.ImmutableList;

public class UserDefinedTypeDef {
    public final UserDefinedTypeName un;
    public final ImmutableList<TypeVariable> ts;
    public final ImmutableList<ConstructorDefinition> cdefs;

    public UserDefinedTypeDef(final UserDefinedTypeName un,
                              final ImmutableList<TypeVariable> ts,
                              final ImmutableList<ConstructorDefinition> cdefs) {
        this.un = un;
        this.ts = ts;
        this.cdefs = cdefs;
    }

    public boolean equals(final Object other) {
        if (other instanceof UserDefinedTypeDef) {
            final UserDefinedTypeDef td = (UserDefinedTypeDef)other;
            return (td.un.equals(un) &&
                    td.ts.equals(ts) &&
                    td.cdefs.equals(cdefs));
        } else {
            return false;
        }
    }
}
