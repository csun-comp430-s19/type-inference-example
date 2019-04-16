package simplescala_experimentation.typechecker;

import java.util.Iterator;

public class TakeIterable<A> implements Iterable<A> {
    public final int takeAmount;
    private final Iterable<A> around;

    public TakeIterable(final int takeAmount, final Iterable<A> around) {
	this.takeAmount = takeAmount;
	this.around = around;
    }

    public Iterator<A> iterator() {
	return new TakeIterator(takeAmount, around.iterator());
    }
}
