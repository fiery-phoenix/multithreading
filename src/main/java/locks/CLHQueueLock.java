package locks;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Andrey Lomakin <a href="mailto:lomakin.andrey@gmail.com">Andrey
 * Lomakin</a>
 * @since 10/5/14
 */
public class CLHQueueLock {

    private final AtomicReference<Qnode> tail = new AtomicReference<Qnode>();

    private final ThreadLocal<Qnode> myNode = new ThreadLocal<Qnode>() {
        @Override
        protected Qnode initialValue() {
            return new Qnode();
        }
    };

    private final ThreadLocal<Qnode> myPred = new ThreadLocal<Qnode>();

    public CLHQueueLock() {
        final Qnode qnode = new Qnode();
        qnode.locked = false;

        tail.set(qnode);
    }

    public void lock() {
        final Qnode localNode = myNode.get();
        localNode.locked = true;

        final Qnode pred = tail.getAndSet(localNode);
        myPred.set(pred);
        pred.parkedThread.set(Thread.currentThread());
        while (pred.locked) {
            LockSupport.park();
        }
    }

    public void unlock() {
        Qnode localMyNode = myNode.get();
        localMyNode.locked = false;
        myNode.set(myPred.get());
        LockSupport.unpark(localMyNode.parkedThread.get());
        releaseReaders(localMyNode);
    }

    public void parkReader(Thread reader) {
        Qnode currentMyNode = myNode.get();
        currentMyNode.readers.add(reader);
        if (locked() && currentMyNode == myNode.get()) {
            LockSupport.park();
        }
    }

    private void releaseReaders(Qnode node) {
        ConcurrentLinkedQueue<Thread> readers = node.readers;
        Thread toUnpark = readers.poll();
        while (toUnpark != null) {
            LockSupport.unpark(toUnpark);
            toUnpark = readers.poll();
        }
    }

    public boolean locked() {
        return tail.get().locked;
    }

    static final class Qnode {

        volatile boolean locked = true;
        final ConcurrentLinkedQueue<Thread> readers = new ConcurrentLinkedQueue<>();
        private final AtomicReference<Thread> parkedThread = new AtomicReference<Thread>();
    }
}
