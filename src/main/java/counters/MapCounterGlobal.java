package counters;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class MapCounterGlobal implements Counter {

	private final AtomicInteger globalInteger = new AtomicInteger();

	private static AtomicInteger nextHashCode = new AtomicInteger();

	private static final int HASH_INCREMENT = 0x61c88647;

	private static int nextHashCode() {
		return nextHashCode.getAndAdd(HASH_INCREMENT);
	}

	private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<Integer>() {

		@Override
		protected Integer initialValue() {
			return nextHashCode();
		}
	};

	private static final byte ATTEMPTS = 8;
	private static final int INITIAL_SIZE = 8;
	private final AtomicBoolean increaseLock = new AtomicBoolean();
	private final AtomicBoolean createLock = new AtomicBoolean();
	private volatile AtomicInteger[] counters;

	public MapCounterGlobal() {
		AtomicInteger[] countersLocal = new AtomicInteger[INITIAL_SIZE];
		//        for (int i = 0; i < INITIAL_SIZE; i++) {
		//            countersLocal[i] = new AtomicInteger();
		//        }
		counters = countersLocal;
	}

	public void inc() {
		int oldGlobal = globalInteger.get();
		if (globalInteger.compareAndSet(oldGlobal, oldGlobal + 1)) {
			return;
		}
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

			//            counter.getAndIncrement();
			//            return;
		}

	}

	public long get() {
		long sum = 0;
		for (AtomicInteger bucket : counters) {
			sum += bucket == null ? 0 : bucket.get();
		}
		return sum + globalInteger.get();
	}

	//    private boolean inc(int counterIndex) {
	//        AtomicInteger counter = counters[counterIndex];
	//        int old = counter.get();
	//        return counter.compareAndSet(old, old + 1);
	//    }
	private void increazeSize() {
		// TODO: check overflow
		int newSize = counters.length << 1;
		AtomicInteger[] newCounters = new AtomicInteger[newSize];
		System.arraycopy(counters, 0, newCounters, 0, counters.length);
		//        for (int i = counters.length; i < newCounters.length; i++) {
		//            newCounters[i] = new AtomicInteger();
		//        }
		counters = newCounters;
	}
}
