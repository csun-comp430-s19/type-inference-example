package simplescala_experimentation.syntax;

import simplescala_experimentation.util.ImmutableList;

public class UserType implements Type {
    public final UserDefinedTypeName un;
    public final ImmutableList<Type> types;

    public UserType(final UserDefinedTypeName un, final ImmutableList<Type> types) {
        this.un = un;
        this.types = types;
    }
    
    public <D, U, E extends Exception> U visitType(final TypeVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof UserType) {
            final UserType ut = (UserType)other;
            return (ut.un.equals(un) &&
                    ut.types.equals(types));
        } else {
            return false;
        }
    }

    public String toString() {
        return un.name + "[" + types.mkString("\n") + "]";
    }
}
