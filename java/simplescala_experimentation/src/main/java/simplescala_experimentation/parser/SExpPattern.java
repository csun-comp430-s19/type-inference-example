package simplescala_experimentation.parser;

import simplescala_experimentation.util.Function0;

public abstract class SExpPattern {
    public SExpPattern() {}
    
    public <A> A ifMatches(final SExp input, final Function0<A> f) {
        if (matches(input)) {
            return f.apply();
        } else {
            throw new SimpleScalaParseException();
        }
    }

    public SExpPattern orElse(final SExpPattern other) {
        final SExpPattern self = this;
        return new SExpPattern() {
            protected void reset() {
                self.reset();
                other.reset();
            }

            public boolean matches(final SExp input) {
                if (self.matches(input)) {
                    return true;
                } else {
                    self.reset();
                    return other.matches(input);
                }
            }
        };
    }
            
    // needed to reset capture patterns
    protected void reset() {}
    
    // ---BEGIN ABSTRACT MEMBERS---
    public abstract boolean matches(SExp input);
    // ---END ABSTRACT MEMBERS---

    public static SExpPattern or(final SExpPattern... patterns) {
        if (patterns.length == 0) {
            throw new SimpleScalaParseException();
        } else {
            SExpPattern retval = patterns[patterns.length - 1];
            for (int x = patterns.length - 2; x >= 0; x--) {
                retval = patterns[x].orElse(retval);
            }
            return retval;
        }
    }
}
