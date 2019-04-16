package simplescala_experimentation.parser;

import simplescala_experimentation.util.Option;
import simplescala_experimentation.util.Some;
import simplescala_experimentation.util.None;

public abstract class PatternFunction<T> extends PartialFunction<T> {
    private final SExpPattern pattern;
    
    public PatternFunction(final SExpPattern pattern) {
        this.pattern = pattern;
    }

    public Option<T> opApply(final SExp input) {
        if (pattern.matches(input)) {
            return new Some<T>(internal());
        } else {
            pattern.reset();
            return new None<T>();
        }
    }

    protected String noMatchExceptionMessage() {
        return pattern.toString();
    }
    
    protected abstract T internal();
}
