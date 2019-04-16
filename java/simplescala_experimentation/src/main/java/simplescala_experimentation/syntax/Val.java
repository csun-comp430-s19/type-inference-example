package simplescala_experimentation.syntax;

public class Val {
    public final Variable x;
    public final Exp e;

    public Val(final Variable x, final Exp e) {
        this.x = x;
        this.e = e;
    }

    public boolean equals(final Object other) {
        if (other instanceof Val) {
            final Val v = (Val)other;
            return v.x.equals(x) && v.e.equals(e);
        } else {
            return false;
        }
    }
}
