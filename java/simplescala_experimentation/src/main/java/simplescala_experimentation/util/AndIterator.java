package simplescala_experimentation.util;

import java.util.Iterator;

public class AndIterator<A> implements Iterator<A> {
    private final Iterator<A> first;
    private final Iterator<A> second;
    
    public AndIterator(final Iterator<A> first, final Iterator<A> second) {
        this.first = first;
        this.second = second;
    }

    public boolean hasNext() {
        return first.hasNext() || second.hasNext();
    }

    public A next() {
        if (first.hasNext()) {
            return first.next();
        } else {
            return second.next();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
