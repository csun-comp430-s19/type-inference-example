package simplescala_experimentation.util;

public class JavaUnit {
    public static final JavaUnit UNIT = new JavaUnit();

    private JavaUnit() {}

    public boolean equals(final Object other) {
        return other instanceof JavaUnit;
    }
}

