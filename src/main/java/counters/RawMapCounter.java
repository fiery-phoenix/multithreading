package counters;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counter that uses thread hashCode and array of AtomicIntegers.
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class RawMapCounter implements Counter {

	private static final int INITIAL_SIZE = 8;
	private static final int MASK = INITIAL_SIZE - 1;

	private volatile AtomicInteger[] counters;

	public RawMapCounter() {
		AtomicInteger[] countersLocal = new AtomicInteger[INITIAL_SIZE];
		for (int i = 0; i < INITIAL_SIZE; i++) {
			countersLocal[i] = new AtomicInteger();
		}
		this.counters = countersLocal;
	}

	public void inc() {
		counters[Thread.currentThread().hashCode() & MASK].incrementAndGet();
	}

	public long get() {
		long sum = 0;
		for (AtomicInteger bucket : counters) {
			sum += bucket.get();
		}
		return sum;
	}

}
