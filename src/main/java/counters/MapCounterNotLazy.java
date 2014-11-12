package counters;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counter with increasing array size.
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class MapCounterNotLazy implements Counter {

	private static final byte ATTEMPTS = 8;
	private static final int INITIAL_SIZE = 8;
	private static final int HASH_INCREMENT = 0x61c88647;

	private static AtomicInteger nextHashCode = new AtomicInteger();

	private static int nextHashCode() {
		return nextHashCode.getAndAdd(HASH_INCREMENT);
	}

	private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<Integer>() {

		@Override
		protected Integer initialValue() {
			return nextHashCode();
		}
	};


	private final AtomicBoolean increaseLock = new AtomicBoolean(false);
	private volatile AtomicInteger[] counters;

	public MapCounterNotLazy() {
		AtomicInteger[] countersLocal = new AtomicInteger[INITIAL_SIZE];
		for (int i = 0; i < INITIAL_SIZE; i++) {
			countersLocal[i] = new AtomicInteger();
		}
		this.counters = countersLocal;
	}

	public void inc() {
		AtomicInteger[] workingCopy = counters;
		int workingMask = workingCopy.length - 1;
		AtomicInteger counter = workingCopy[THREAD_LOCAL.get() & workingMask];
		for (byte i = 0; i < ATTEMPTS; i++) {
			int old = counter.get();
			if (counter.compareAndSet(old, old + 1)) {
				return;
			}
		}
		if (increaseLock.compareAndSet(false, true)) {
			increaseSize();
			increaseLock.lazySet(false);
		}
		counter.incrementAndGet();
	}

	public long get() {
		long sum = 0;
		for (AtomicInteger bucket : counters) {
			sum += bucket.get();
		}
		return sum;
	}

	private void increaseSize() {
		// TODO: check overflow
		int newSize = counters.length << 1;
		AtomicInteger[] newCounters = new AtomicInteger[newSize];
		System.arraycopy(counters, 0, newCounters, 0, counters.length);
		for (int i = counters.length; i < newCounters.length; i++) {
			newCounters[i] = new AtomicInteger();
		}
		counters = newCounters;
	}
}
