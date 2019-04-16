package simplescala_experimentation.util;

public interface Option<A> extends Iterable<A> {
    public boolean isDefined();
    public A get();
}
