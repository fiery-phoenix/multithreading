package counters;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counter that uses more suitable thread local hashCode.
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class MapCounterWithTLHash implements Counter {

	private static final int INITIAL_SIZE = 8;
	private static final int MASK = 7;

	private static final int HASH_INCREMENT = 0x61c88647;

	private static AtomicInteger nextHashCode = new AtomicInteger();

	private static int nextHashCode() {
		return nextHashCode.getAndAdd(HASH_INCREMENT);
	}

	private static ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<Integer>() {

		@Override
		protected Integer initialValue() {
			return nextHashCode();
		}
	};

	private volatile AtomicInteger[] counters;

	public MapCounterWithTLHash() {
		AtomicInteger[] countersLocal = new AtomicInteger[INITIAL_SIZE];
		for (int i = 0; i < INITIAL_SIZE; i++) {
			countersLocal[i] = new AtomicInteger();
		}
		this.counters = countersLocal;
	}

	public void inc() {
		counters[THREAD_LOCAL.get() & MASK].incrementAndGet();
	}

	public long get() {
		long sum = 0;
		for (AtomicInteger bucket : counters) {
			sum += bucket.get();
		}
		return sum;
	}

}
