package simplescala_experimentation.syntax;

public class TypeVariableType implements Type {
    public final TypeVariable tv;

    public TypeVariableType(final TypeVariable tv) {
        this.tv = tv;
    }
    
    public <D, U, E extends Exception> U visitType(final TypeVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return (other instanceof TypeVariableType &&
                ((TypeVariableType)other).tv.equals(tv));
    }

    public String toString() {
        return tv.name;
    }
}
