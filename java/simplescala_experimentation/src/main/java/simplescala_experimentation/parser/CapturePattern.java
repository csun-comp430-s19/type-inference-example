package simplescala_experimentation.parser;

public abstract class CapturePattern<A> extends SExpPattern {
    private A capture;
    
    public CapturePattern() {
        capture = null;
    }

    protected boolean setCapture(final A a) {
        if (capture == null) {
            capture = a;
            return true;
        } else {
            throw new MatchException("Capture already occurred");
        }
    }

    public A getCapture() {
        if (capture == null) {
            throw new MatchException("Capture never performed");
        } else {
            return capture;
        }
    }

    public boolean matches(final SExp input) {
        return input.visitSExp(getVisitor());
    }

    protected void reset() {
        capture = null;
    }

    public String toString() {
        return "CapturePattern()";
    }
    
    protected abstract MatchVisitor getVisitor();
}
