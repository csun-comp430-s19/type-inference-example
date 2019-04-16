package simplescala_experimentation.syntax;

import simplescala_experimentation.util.ImmutableList;

public class Def {
    public final FunctionName fn;
    public final ImmutableList<TypeVariable> ts;
    public final Variable x;
    public final Type tau1;
    public final Type tau2;
    public final Exp e;

    public Def(final FunctionName fn,
               final ImmutableList<TypeVariable> ts,
               final Variable x,
               final Type tau1,
               final Type tau2,
               final Exp e) {
        this.fn = fn;
        this.ts = ts;
        this.x = x;
        this.tau1 = tau1;
        this.tau2 = tau2;
        this.e = e;
    }

    public boolean equals(final Object other) {
        if (other instanceof Def) {
            final Def d = (Def)other;
            return (d.fn.equals(fn) &&
                    d.ts.equals(ts) &&
                    d.x.equals(x) &&
                    d.tau1.equals(tau1) &&
                    d.tau2.equals(tau2) &&
                    d.e.equals(e));
        } else {
            return false;
        }
    }
}
