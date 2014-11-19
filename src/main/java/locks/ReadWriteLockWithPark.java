package locks;

import counters.MapCounterLazy;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class ReadWriteLockWithPark {

    private final AtomicInteger writeCount = new AtomicInteger();
    private final MapCounterLazy readCount = new MapCounterLazy();
    private final ConcurrentLinkedQueue<Thread> readers = new ConcurrentLinkedQueue<>();

    public void acquireReadLock() {
        while (true) {
//            LockSupport.unpark(Thread.currentThread());
            readCount.inc();

            if (writeCount.get() == 0) {
                return;
            }

            readCount.dec();
            
            readers.add(Thread.currentThread());
            if (writeCount.get() > 0) {
                LockSupport.park();
            }
//            readers.remove(Thread.currentThread());
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
