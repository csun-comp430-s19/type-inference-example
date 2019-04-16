package simplescala_experimentation.util;

public class ImmutableSet<A> {
    public final ImmutableMap<A, JavaUnit> map;

    public ImmutableSet(final ImmutableMap<A, JavaUnit> map) {
        this.map = map;
    }
    
    public ImmutableSet() {
        this(new ImmutableMap<A, JavaUnit>());
    }

    public ImmutableSet<A> add(final A element) {
        return new ImmutableSet<A>(map.add(element, JavaUnit.UNIT));
    }

    public boolean contains(final A element) {
        return map.contains(element);
    }

    public int size() {
        return map.size();
    }

    @SuppressWarnings("unchecked")
    public boolean equals(final Object other) {
        if (other instanceof ImmutableSet<?>) {
            return equals((ImmutableSet<A>)other);
        } else {
            return false;
        }
    }

    public boolean equals(final ImmutableSet<A> other) {
        return map.equals(other.map);
    }
    
    public static <A> ImmutableSet<A> makeSet(final ImmutableList<A> list) {
        ImmutableSet<A> retval = new ImmutableSet<A>();
        for (final A a : list) {
            retval = retval.add(a);
        }
        return retval;
    }

    public ImmutableList<A> toList() {
        return map.pairs.map(new Function1<Pair<A, JavaUnit>, A>() {
                public A apply(final Pair<A, JavaUnit> pair) {
                    return pair.first;
                }
            });
    }
}
