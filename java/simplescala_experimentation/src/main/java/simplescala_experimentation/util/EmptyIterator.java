package simplescala_experimentation.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptyIterator<A> implements Iterator<A> {
    public boolean hasNext() {
        return false;
    }

    public A next() {
        throw new NoSuchElementException("No elements on an empty iterator");
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
