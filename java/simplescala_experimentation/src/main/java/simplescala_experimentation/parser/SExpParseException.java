package simplescala_experimentation.parser;

public class SExpParseException extends RuntimeException {
    public SExpParseException(final String message) {
        super(message);
    }

    public SExpParseException() {
        super();
    }
}
