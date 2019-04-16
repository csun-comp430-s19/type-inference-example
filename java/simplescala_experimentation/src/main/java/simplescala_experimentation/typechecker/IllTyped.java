package simplescala_experimentation.typechecker;

public class IllTyped extends RuntimeException {
    public IllTyped(final String message) {
        super(message);
    }

    public IllTyped() {
        super();
    }
}
