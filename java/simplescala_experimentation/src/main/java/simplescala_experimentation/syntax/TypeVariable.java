package simplescala_experimentation.syntax;

public class TypeVariable {
    public final String name;
    public TypeVariable(final String name) {
        this.name = name;
    }

    public boolean equals(final Object other) {
        return (other instanceof TypeVariable &&
                ((TypeVariable)other).name.equals(name));
    }
}
