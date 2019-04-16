package simplescala_experimentation.util;

public class ImmutableMap<K, V> {
    public final ImmutableList<Pair<K, V>> pairs;

    public ImmutableMap(ImmutableList<Pair<K, V>> pairs) {
        this.pairs = pairs;
    }

    public ImmutableMap() {
        this(new Nil<Pair<K, V>>());
    }

    public boolean contains(final K key) {
        return get(key).isDefined();
    }
    
    public Option<V> get(final K key) {
        for (final Pair<K, V> pair : pairs) {
            if (pair.first.equals(key)) {
                return new Some<V>(pair.second);
            }
        }
        return new None<V>();
    }

    @SuppressWarnings("unchecked")
    public boolean equals(final Object other) {
        if (other instanceof ImmutableMap<?, ?>) {
            return equals((ImmutableMap<K, V>)other);
        } else {
            return false;
        }
    }

    public boolean equals(final ImmutableMap<K, V> other) {
        // ensure that each key/value pair is there
        return (pairs.size() == other.pairs.size() &&
                pairs.forall(new Predicate<Pair<K, V>>() {
                        public boolean matches(final Pair<K, V> pair) {
                            final Option<V> otherV = other.get(pair.first);
                            return (otherV.isDefined() &&
                                    otherV.get().equals(pair.second));
                        }
                    }));
    }

    public ImmutableMap<K, V> multiAdd(final ImmutableList<Pair<K, V>> pairs) {
        ImmutableMap<K, V> retval = this;
        for (final Pair<K, V> pair : pairs) {
            retval = retval.add(pair.first, pair.second);
        }
        return retval;
    }
    
    public ImmutableMap<K, V> add(final K key, final V value) {
        // pull out the old mapping if it's there, and add this one
        final Pair<Option<Pair<K, V>>, ImmutableList<Pair<K, V>>> result =
            pairs.select(new Predicate<Pair<K, V>>() {
                    public boolean matches(final Pair<K, V> pair) {
                        return pair.first.equals(key);
                    }
                });
        return new ImmutableMap<K, V>(new Cons<Pair<K, V>>(new Pair<K, V>(key, value),
                                                           result.second));
    }

    public int size() {
        return pairs.size();
    }

    public ImmutableSet<K> keySet() {
        return ImmutableSet.makeSet(pairs.map(new Function1<Pair<K, V>, K>() {
                public K apply(final Pair<K, V> pair) {
                    return pair.first;
                }
            }));
    }

    public Option<Pair<K, V>> find(final Predicate<Pair<K, V>> p) {
        return pairs.find(p);
    }
}
