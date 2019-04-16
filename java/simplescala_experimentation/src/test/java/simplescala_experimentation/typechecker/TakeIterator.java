package simplescala_experimentation.typechecker;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TakeIterator<A> implements Iterator<A> {
    public final int takeAmount;
    private final Iterator<A> around;
    private int numTaken;
    
    public TakeIterator(final int takeAmount, final Iterator<A> around) {
	this.takeAmount = takeAmount;
	this.around = around;
	numTaken = 0;
    }

    public boolean hasNext() {
	return numTaken < takeAmount && around.hasNext();
    }

    public A next() {
	if (!hasNext()) {
	    throw new NoSuchElementException();
	}
	numTaken++;
	return around.next();
    }

    public void remove() {
	around.remove();
    }
}
