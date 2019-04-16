package simplescala_experimentation.syntax;

public class ConstructorCase implements Case {
    public final ConstructorName cn;
    public final Variable x;
    public final Exp e;

    public ConstructorCase(final ConstructorName cn,
                           final Variable x,
                           final Exp e) {
        this.cn = cn;
        this.x = x;
        this.e = e;
    }

    public <D, U, E extends Exception> U visitCase(final CaseVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof ConstructorCase) {
            final ConstructorCase cc = (ConstructorCase)other;
            return (cc.cn.equals(cn) &&
                    cc.x.equals(x) &&
                    cc.e.equals(e));
        } else {
            return false;
        }
    }
}
