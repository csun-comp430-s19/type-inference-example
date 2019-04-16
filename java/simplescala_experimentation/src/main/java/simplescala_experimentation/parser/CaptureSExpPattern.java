package simplescala_experimentation.parser;

import simplescala_experimentation.util.ImmutableList;

public class CaptureSExpPattern extends CapturePattern<SExp> {
    // this cannot be used as a parameter because of initialization
    // order woes
    private final MatchVisitor visitor;
    public CaptureSExpPattern() {
        super();
        visitor = new MatchVisitor() {
                public boolean visit(final String atom) {
                    return setCapture(new SExpAtom(atom));
                }
                public boolean visit(final ImmutableList<SExp> list) {
                    return setCapture(new SExpList(list));
                }
            };
    }

    protected MatchVisitor getVisitor() { return visitor; }
}
