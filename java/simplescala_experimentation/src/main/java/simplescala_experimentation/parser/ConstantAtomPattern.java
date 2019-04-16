package simplescala_experimentation.parser;

public class ConstantAtomPattern extends SExpPattern {
    public final String expect;
    public ConstantAtomPattern(final String expect) {
        this.expect = expect;
    }

    public boolean matches(final SExp input) {
        return input.visitSExp(new MatchVisitor() {
                public boolean visit(final String atom) {
                    return atom.equals(expect);
                }
            }).booleanValue();
    }

    public String toString() {
        return "ConstantAtomPattern(" + expect + ")";
    }
}
