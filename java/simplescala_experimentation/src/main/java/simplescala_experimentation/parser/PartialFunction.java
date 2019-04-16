package simplescala_experimentation.parser;

import simplescala_experimentation.util.Option;

public abstract class PartialFunction<T> {
    public abstract Option<T> opApply(final SExp input);

    public T apply(final SExp input) {
        final Option<T> result = opApply(input);
        if (result.isDefined()) {
            return result.get();
        } else {
            throw new SimpleScalaParseException(input.toString() + " WITH " + noMatchExceptionMessage());
        }
    }

    protected String noMatchExceptionMessage() {
        return "";
    }
    
    public PartialFunction<T> orElse(final PartialFunction<T> other) {
        final PartialFunction<T> self = this;
        return new PartialFunction<T>() {
            public Option<T> opApply(final SExp input) {
                final Option<T> selfResult = self.opApply(input);
                if (selfResult.isDefined()) {
                    return selfResult;
                } else {
                    return other.opApply(input);
                }
            }

            protected String noMatchExceptionMessage() {
                return (self.noMatchExceptionMessage() + " OR " +
                        other.noMatchExceptionMessage());
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> PartialFunction<T> or(final PartialFunction<T>... functions) {
        if (functions.length == 0) {
            throw new SimpleScalaParseException();
        } else {
            PartialFunction<T> retval = functions[functions.length - 1];
            for (int x = functions.length - 2; x >= 0; x--) {
                retval = functions[x].orElse(retval);
            }
            return retval;
        }
    }
}
