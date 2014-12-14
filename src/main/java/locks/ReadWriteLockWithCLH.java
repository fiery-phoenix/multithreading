package locks;

import counters.MapCounterForLock;

/**
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class ReadWriteLockWithCLH {

    private final CLHQueueLock writeCount = new CLHQueueLock();
    private final MapCounterForLock readCount = new MapCounterForLock();

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
