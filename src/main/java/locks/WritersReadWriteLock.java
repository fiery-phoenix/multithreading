package locks;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class WritersReadWriteLock {

	private final AtomicInteger writeCount = new AtomicInteger();
	private final AtomicInteger readCount = new AtomicInteger();

	public void acquireReadLock() {
		if (writeCount.get() == 0) {
			int old = readCount.get();

			while (old > 0) {
				if (readCount.compareAndSet(old, old + 1)) {
					return;
				}
				old = readCount.get();
			}
		}

		while (!writeCount.compareAndSet(0, 1)) {
		}
		readCount.incrementAndGet();
		writeCount.set(0);

	}

	public void releaseReadLock() {
		while (!decrementCounter(readCount)) {
		}

	}

	public void acquireWriteLock() {
		while (!writeCount.compareAndSet(0, 1)) {
		}

		while (readCount.get() > 0) {
		}
	}

	public void releaseWriteLock() {
		writeCount.set(0);
	}

	private boolean decrementCounter(AtomicInteger i) {
		int old = i.get();
		return i.compareAndSet(old, old == 0 ? 0 : old - 1);
	}
}
