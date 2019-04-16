package simplescala_experimentation.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingletonIterator<A> implements Iterator<A> {
    private final A element;
    private boolean hasElement;

    public SingletonIterator(final A element) {
        this.element = element;
        hasElement = true;
    }

    public boolean hasNext() {
        return hasElement;
    }

    public A next() {
        if (hasElement) {
            hasElement = false;
            return element;
        } else {
            throw new NoSuchElementException("Already got element");
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
