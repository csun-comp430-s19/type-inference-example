package simplescala_experimentation.util;

import java.util.Iterator;

public class Cons<A> extends ImmutableList<A> {
    public final A head;
    public final ImmutableList<A> tail;
    
    public Cons(final A head, final ImmutableList<A> tail) {
        this.head = head;
        this.tail = tail;
    }

    public boolean isEmpty() {
        return false;
    }

    public Iterator<A> iterator() {
        return new AndIterator<A>(new SingletonIterator<A>(head),
                                  tail.iterator());
    }

    public int size() {
        return 1 + tail.size();
    }

    public Pair<Option<A>, ImmutableList<A>> select(Predicate<A> p) {
        if (p.matches(head)) {
            return new Pair<Option<A>, ImmutableList<A>>(new Some<A>(head), tail);
        } else {
            final Pair<Option<A>, ImmutableList<A>> rest = tail.select(p);
            return new Pair<Option<A>, ImmutableList<A>>(rest.first,
                                                         new Cons<A>(head, rest.second));
        }
    }

    public <B> ImmutableList<B> map(final Function1<A, B> f) {
        return new Cons<B>(f.apply(head), tail.map(f));
    }

    public A first() {
        return head;
    }

    public ImmutableList<A> rest() {
        return tail;
    }

    public ImmutableList<A> append(final ImmutableList<A> other) {
        return new Cons<A>(head,
                           tail.append(other));
    }
}
