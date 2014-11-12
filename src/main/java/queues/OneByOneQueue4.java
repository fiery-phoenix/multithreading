package queues;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import sun.misc.Contended;

/**
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class OneByOneQueue4<E> extends AbstractQueue<E> {

	private final E[] elements;

	private final AtomicLong addIndex = new AtomicLong();
	private final AtomicLong getIndex = new AtomicLong();
	@Contended
	private long knownAddIndex;
	@Contended
	private long knownGetIndex;

	private final int capacity;
	private final int bitMask;

	public OneByOneQueue4(int capacity) {
		int actualCapacity = Integer.bitCount(capacity) == 1 ? capacity : Integer.highestOneBit(capacity);
		this.capacity = actualCapacity;
		bitMask = actualCapacity - 1;
		elements = (E[]) new Object[actualCapacity];
	}

	@Override
	public boolean offer(E e) {
		long currentAdd = addIndex.get();
		if (currentAdd - knownGetIndex >= capacity) {
			knownGetIndex = getIndex.get();
			if (currentAdd - knownGetIndex >= capacity) {
				return false;
			}
		}

		elements[(int) (currentAdd & bitMask)] = e;
		addIndex.lazySet(currentAdd + 1);

		return true;
	}

	@Override
	public E poll() {
		long currentGet = getIndex.get();
		if (knownAddIndex <= currentGet) {
			knownAddIndex = addIndex.get();
			if (knownAddIndex <= currentGet) {
				return null;
			}
		}
		E e = elements[(int) (currentGet & bitMask)];
		getIndex.lazySet(currentGet + 1);
		return e;
	}

	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public E peek() {
		throw new UnsupportedOperationException();
	}
}
