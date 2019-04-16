package simplescala_experimentation.syntax;

public class TypePlaceholder implements Type {
    public final int placeholder;

    public TypePlaceholder(final int placeholder) {
        this.placeholder = placeholder;
    }
    
    public <D, U, E extends Exception> U visitType(final TypeVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        return (other instanceof TypePlaceholder &&
                ((TypePlaceholder)other).placeholder == placeholder);
    }

    public String toString() {
        return "._" + placeholder;
    }
}
