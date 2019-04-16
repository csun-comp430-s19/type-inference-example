package simplescala_experimentation.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Nil<A> extends ImmutableList<A> {
    public boolean isEmpty() {
        return true;
    }

    public Iterator<A> iterator() {
        return new EmptyIterator<A>();
    }

    public int size() {
        return 0;
    }

    public Pair<Option<A>, ImmutableList<A>> select(Predicate<A> p) {
        return new Pair<Option<A>, ImmutableList<A>>(new None<A>(), this);
    }

    public <B> ImmutableList<B> map(final Function1<A, B> f) {
        return new Nil<B>();
    }

    public A first() {
        throw new NoSuchElementException();
    }

    public ImmutableList<A> rest() {
        throw new NoSuchElementException();
    }

    public ImmutableList<A> append(final ImmutableList<A> other) {
        return other;
    }
}
