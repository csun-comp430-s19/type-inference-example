package simplescala_experimentation.util;

public class Pair<A, B> {
    public final A first;
    public final B second;

    public Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }
    
    @SuppressWarnings("unchecked")
    public boolean equals(final Object other) {
        if (other instanceof Pair<?, ?>) {
            final Pair<A, B> pair = (Pair<A, B>)other;
            return (pair.first.equals(first) &&
                    pair.second.equals(second));
        } else {
            return false;
        }
    }
}
