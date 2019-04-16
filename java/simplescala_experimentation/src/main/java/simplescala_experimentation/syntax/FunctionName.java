package simplescala_experimentation.syntax;

public class FunctionName {
    public final String name;
    public FunctionName(final String name) {
        this.name = name;
    }

    public boolean equals(final Object other) {
        return (other instanceof FunctionName &&
                ((FunctionName)other).name.equals(name));
    }
}
