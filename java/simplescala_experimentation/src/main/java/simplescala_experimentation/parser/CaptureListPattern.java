package simplescala_experimentation.parser;

import simplescala_experimentation.util.ImmutableList;

public class CaptureListPattern extends CapturePattern<ImmutableList<SExp>> {
    // this cannot be used as a parameter because of initialization
    // order woes
    private final MatchVisitor visitor;
    public CaptureListPattern() {
        super();
        visitor = new MatchVisitor() {
                public boolean visit(final ImmutableList<SExp> list) {
                    return setCapture(list);
                }
            };
    }

    protected MatchVisitor getVisitor() { return visitor; }
}
