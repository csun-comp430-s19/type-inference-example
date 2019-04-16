package simplescala_experimentation.util;

import java.util.Iterator;

public abstract class ImmutableList<A> implements Iterable<A> {
    // ---BEGIN ABSTRACT MEMBERS---
    public abstract boolean isEmpty();
    public abstract int size();
    
    // gets the first element if there is one, and returns the rest of the
    // elements in the list
    public abstract Pair<Option<A>, ImmutableList<A>> select(Predicate<A> p);

    public abstract <B> ImmutableList<B> map(Function1<A, B> f);
    
    public abstract A first();
    public abstract ImmutableList<A> rest();
    public abstract ImmutableList<A> append(ImmutableList<A> other);
    // ---END ABSTRACT MEMBERS---
    
    @SuppressWarnings("unchecked")
    public boolean equals(final Object other) {
        if (other instanceof ImmutableList<?>) {
            return equals((ImmutableList<A>) other);
        } else {
            return false;
        }
    }

    public static <A, B> ImmutableList<Pair<A, B>> zip(final Iterator<A> as, final Iterator<B> bs) {
        if (as.hasNext() && bs.hasNext()) {
            return new Cons<Pair<A, B>>(new Pair<A, B>(as.next(), bs.next()),
                                        zip(as, bs));
        } else {
            return new Nil<Pair<A, B>>();
        }
    }
    
    public <B> ImmutableList<Pair<A, B>> zip(final ImmutableList<B> other) {
        return zip(iterator(), other.iterator());
    }
    
    public boolean equals(final ImmutableList<A> other) {
        if (size() != other.size()) {
            return false;
        } else {
            for (final Pair<A, A> pair : zip(other)) {
                if (!pair.first.equals(pair.second)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean exists(final Predicate<A> p) {
        return select(p).first.isDefined();
    }

    public boolean forall(final Predicate<A> p) {
        return !exists(new Predicate<A>() {
                public boolean matches(final A a) {
                    return !p.matches(a);
                }
            });
    }
    
    @SuppressWarnings("unchecked")
    public static <A> ImmutableList<A> makeList(final A... as) {
        ImmutableList<A> retval = new Nil<A>();
        for (int x = as.length - 1; x >= 0; x--) {
            retval = new Cons<A>(as[x], retval);
        }
        return retval;
    }

    public Option<A> find(final Predicate<A> p) {
        return select(p).first;
    }

    public ImmutableList<A> reverse() {
        ImmutableList<A> retval = new Nil<A>();
        for (final A a : this) {
            retval = new Cons<A>(a, retval);
        }
        return retval;
    }

    public ImmutableSet<A> toSet() {
        return ImmutableSet.makeSet(this);
    }

    public <B> ImmutableList<B> flatMap(final Function1<A, ImmutableList<B>> f) {
        ImmutableList<B> retval = new Nil<B>();
        for (final ImmutableList<B> bList : map(f).reverse()) {
            retval = bList.append(retval);
        }
        return retval;
    }

    public String mkString(final String separator) {
        final StringBuilder retval = new StringBuilder();
        final Iterator<A> it = iterator();

        while (it.hasNext()) {
            retval.append(it.next().toString());
            if (it.hasNext()) {
                retval.append(separator);
            }
        }

        return retval.toString();
    }

    public String mkString() {
        return mkString("");
    }
            
    public static <A, B> Pair<ImmutableList<A>, ImmutableList<B>> unzip(final ImmutableList<Pair<A, B>> pairs) {
        ImmutableList<A> as = new Nil<A>();
        ImmutableList<B> bs = new Nil<B>();

        for (final Pair<A, B> pair : pairs) {
            as = new Cons<A>(pair.first, as);
            bs = new Cons<B>(pair.second, bs);
        }

        return new Pair<ImmutableList<A>, ImmutableList<B>>(as.reverse(), bs.reverse());
    }

    public String toString() {
        return "ImmutableList(" + mkString(", ") + ")";
    }
}

