package simplescala_experimentation.parser;

public class CaptureAtomPattern extends CapturePattern<String> {
    // this cannot be used as a parameter because of initialization
    // order woes
    private final MatchVisitor visitor;
    public CaptureAtomPattern() {
        super();
        visitor = new MatchVisitor() {
                public boolean visit(final String atom) {
                    return setCapture(atom);
                }
            };
    }

    protected MatchVisitor getVisitor() { return visitor; }
}
