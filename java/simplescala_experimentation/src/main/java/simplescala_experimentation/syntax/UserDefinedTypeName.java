package simplescala_experimentation.syntax;

public class UserDefinedTypeName {
    public final String name;
    public UserDefinedTypeName(final String name) {
        this.name = name;
    }

    public boolean equals(final Object other) {
        return (other instanceof UserDefinedTypeName &&
                ((UserDefinedTypeName)other).name.equals(name));
    }
}
