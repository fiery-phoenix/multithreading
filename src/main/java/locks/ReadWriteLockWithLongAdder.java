package locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class ReadWriteLockWithLongAdder {

	private final AtomicInteger writeCount = new AtomicInteger();
	private final LongAdder readCount = new LongAdder();

	public void acquireReadLock() {
		readCount.increment();
		if (writeCount.get() == 0) {
			return;
		}
		readCount.decrement();

		while (!writeCount.compareAndSet(0, 1)) {
		}
		readCount.increment();
		writeCount.set(0);
	}

	public void releaseReadLock() {
		readCount.decrement();
	}

	public void acquireWriteLock() {
		while (!writeCount.compareAndSet(0, 1)) {
		}

		while (readCount.sum() > 0) {
		}
	}

	public void releaseWriteLock() {
		writeCount.set(0);
	}

}
