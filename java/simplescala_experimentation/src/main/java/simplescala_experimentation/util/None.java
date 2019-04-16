package simplescala_experimentation.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class None<A> implements Option<A> {
    public None() {}

    public boolean isDefined() {
        return false;
    }

    public Iterator<A> iterator() {
        return new EmptyIterator<A>();
    }

    public A get() {
        throw new NoSuchElementException();
    }

    public boolean equals(final Object other) {
        return other instanceof None<?>;
    }
}
