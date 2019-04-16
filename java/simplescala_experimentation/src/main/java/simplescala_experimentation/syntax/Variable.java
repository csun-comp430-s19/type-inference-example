package simplescala_experimentation.syntax;

public class Variable {
    public final String name;
    public Variable(final String name) {
        this.name = name;
    }

    public boolean equals(final Object other) {
        return (other instanceof Variable &&
                ((Variable)other).name.equals(name));
    }
}
