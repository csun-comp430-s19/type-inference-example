package simplescala_experimentation.syntax;

public class ConstructorDefinition {
    public final ConstructorName cn;
    public final Type tau;

    public ConstructorDefinition(final ConstructorName cn, final Type tau) {
        this.cn = cn;
        this.tau = tau;
    }

    public boolean equals(final Object other) {
        if (other instanceof ConstructorDefinition) {
            final ConstructorDefinition cd = (ConstructorDefinition)other;
            return cd.cn.equals(cn) && cd.tau.equals(tau);
        } else {
            return false;
        }
    }
}
