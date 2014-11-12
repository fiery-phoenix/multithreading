package counters;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counter with lazy initialization of array elements.
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class MapCounterLazy implements Counter {

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

	private final AtomicBoolean increaseLock = new AtomicBoolean();
	private volatile AtomicInteger[] counters;

	public MapCounterLazy() {
		counters = new AtomicInteger[INITIAL_SIZE];
	}

	public void inc() {
		while (true) {
			AtomicInteger[] workingCopy = counters;
			int workingMask = workingCopy.length - 1;
			int counterIndex = THREAD_LOCAL.get() & workingMask;
			AtomicInteger counter = workingCopy[counterIndex];
			if (counter == null) {
				if (!increaseLock.get() && increaseLock.compareAndSet(false, true)) {
					if (workingCopy == counters && counters[counterIndex] == null) {
						workingCopy[counterIndex] = new AtomicInteger(1);
						increaseLock.lazySet(false);
						return;
					}
					continue;
				}
				continue;
			}

			for (byte i = 0; i < ATTEMPTS; i++) {
				int old = counter.get();
				if (counter.compareAndSet(old, old + 1)) {
					return;
				}
			}

			if (!increaseLock.get() && increaseLock.compareAndSet(false, true)) {
				if (counters == workingCopy) {
					increazeSize();
					increaseLock.lazySet(false);
				}
			}
		}
	}

	public long get() {
		long sum = 0;
		for (AtomicInteger bucket : counters) {
			sum += bucket == null ? 0 : bucket.get();
		}
		return sum;
	}

	private void increazeSize() {
		// TODO: check overflow
		int newSize = counters.length << 1;
		AtomicInteger[] newCounters = new AtomicInteger[newSize];
		System.arraycopy(counters, 0, newCounters, 0, counters.length);
		counters = newCounters;
	}
}
