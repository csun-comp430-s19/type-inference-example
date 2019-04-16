package simplescala_experimentation.parser;

public class SimpleScalaParseException extends RuntimeException {
    public SimpleScalaParseException(final String message) {
        super(message);
    }

    public SimpleScalaParseException() {
        super();
    }
}
