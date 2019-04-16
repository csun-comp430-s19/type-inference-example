package simplescala_experimentation.util;

import java.util.Iterator;

public class Some<A> implements Option<A> {
    public final A element;

    public Some(final A element) {
        this.element = element;
    }

    public boolean isDefined() {
        return true;
    }

    public Iterator<A> iterator() {
        return new SingletonIterator<A>(element);
    }

    public A get() {
        return element;
    }

    @SuppressWarnings("unchecked")
    public boolean equals(final Object other) {
        return (other instanceof Some<?> &&
                ((Some<A>)other).element.equals(element));
    }
}
