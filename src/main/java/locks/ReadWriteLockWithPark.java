package locks;

import counters.MapCounterForLock;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class ReadWriteLockWithPark {

    private final AtomicInteger writeCount = new AtomicInteger();
    private final MapCounterForLock readCount = new MapCounterForLock();
    private final ConcurrentLinkedQueue<Thread> readers = new ConcurrentLinkedQueue<>();

    public void acquireReadLock() {
        while (true) {
            readCount.inc();

            if (writeCount.get() == 0) {
                return;
            }

            readCount.dec();
            
            readers.add(Thread.currentThread());
            if (writeCount.get() > 0) {
                LockSupport.park();
            }
        }
    }

    public void releaseReadLock() {
        readCount.dec();
    }

    public void acquireWriteLock() {
        while (!writeCount.compareAndSet(0, 1)) {
        }

        while (readCount.get() > 0) {
        }
    }

    public void releaseWriteLock() {
        writeCount.set(0);
        Thread toUnpark = readers.poll();
        while (toUnpark != null) {
            LockSupport.unpark(toUnpark);
            toUnpark = readers.poll();
        }
    }

}
