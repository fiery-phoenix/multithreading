package locks;

import counters.MapCounterLazy;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class ReadWriteLockWithCLH {

    private final CLHQueueLock writeCount = new CLHQueueLock();
    private final MapCounterLazy readCount = new MapCounterLazy();

    public void acquireReadLock() {
        while (true) {
            readCount.inc();

            if (!writeCount.locked()) {
                return;
            }

            readCount.dec();
            
            writeCount.parkReader(Thread.currentThread());
        }
    }

    public void releaseReadLock() {
        readCount.dec();
    }

    public void acquireWriteLock() {
       writeCount.lock();

        while (readCount.get() > 0) {
        }
    }

    public void releaseWriteLock() {
        writeCount.unlock();
    }

}
