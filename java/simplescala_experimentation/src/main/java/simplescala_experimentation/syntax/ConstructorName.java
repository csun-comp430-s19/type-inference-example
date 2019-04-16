package simplescala_experimentation.syntax;

public class ConstructorName {
    public final String name;
    public ConstructorName(final String name) {
        this.name = name;
    }

    public boolean equals(final Object other) {
        return (other instanceof ConstructorName &&
                ((ConstructorName)other).name.equals(name));
    }
}

    
