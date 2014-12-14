package collections;

import java.util.AbstractList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author Kseniya Panasyuk (KPanasiuk@luxoft.com)
 */
public class LockFreeList<E> extends AbstractList<E> {

	private static final int FIRST_BUCKET_SIZE = 2;

	private final AtomicReferenceArray<AtomicReferenceArray<E>> arrays = new AtomicReferenceArray<>(32);
	private final AtomicReference<Descriptor<E>> descriptorRef = new AtomicReference<>();

	private static class Descriptor<E> {
		private final int size;
		private final WriteOperation<E> writeOperation;

		public Descriptor(int size, WriteOperation<E> writeOperation) {
			this.size = size;
			this.writeOperation = writeOperation;
		}
	}

	private static class WriteOperation<E> {
		private final E newValue;
		private final int index;
		private AtomicBoolean pending = new AtomicBoolean(true);

		public WriteOperation(E newValue, int index) {
			this.newValue = newValue;
			this.index = index;
		}

		public WriteOperation(E newValue, int index, AtomicBoolean pending) {
			this.newValue = newValue;
			this.index = index;
			this.pending = pending;
		}
	}

	public LockFreeList() {
		arrays.set(0, new AtomicReferenceArray<>(FIRST_BUCKET_SIZE));
		descriptorRef.set(new Descriptor<>(0, new WriteOperation<>(null, 0, new AtomicBoolean(false))));
	}

	@Override
	public boolean add(E e) {
		Descriptor<E> dCurrent;
		Descriptor<E> dNext;
		do {
			dCurrent = descriptorRef.get();
			completeWrite(dCurrent.writeOperation);
			int i = dCurrent.size + FIRST_BUCKET_SIZE;
			int highestBit = Integer.highestOneBit(i);

			int bucket = Integer.numberOfTrailingZeros(highestBit) - 1;
			if (arrays.get(bucket) == null) {
				allocateBucket(bucket);
			}
			WriteOperation<E> writeop = new WriteOperation<>(e, dCurrent.size);
			dNext = new Descriptor<>(dCurrent.size + 1, writeop);
		} while (!descriptorRef.compareAndSet(dCurrent, dNext));
		completeWrite(dNext.writeOperation);

		return true;
	}

	public void completeWrite(WriteOperation<E> writeOperation) {
		if (writeOperation.pending.get()) {
			int index = writeOperation.index;
			int i = index + FIRST_BUCKET_SIZE;
			int highestBit = Integer.highestOneBit(i);

			int bucket = Integer.numberOfTrailingZeros(highestBit) - 1;
			int position = i ^ highestBit;

			arrays.get(bucket).compareAndSet(position, null, writeOperation.newValue);
			writeOperation.pending.set(false);
		}
	}

	private void allocateBucket(int bucket) {
		int size = 2 << bucket;
		arrays.compareAndSet(bucket, null, new AtomicReferenceArray<>(size));
	}

	@Override
	public E get(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		int i = index + FIRST_BUCKET_SIZE;
		int highestBit = Integer.highestOneBit(i);

		int bucket = Integer.numberOfTrailingZeros(highestBit) - 1;
		int position = i ^ highestBit;

		return arrays.get(bucket).get(position);
	}

	@Override
	public E set(int index, E element) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		int i = index + FIRST_BUCKET_SIZE;
		int highestBit = Integer.highestOneBit(i);

		int bucket = Integer.numberOfTrailingZeros(highestBit) - 1;
		int position = i ^ highestBit;

		return arrays.get(bucket).getAndSet(position, element);
	}

	@Override
	public int size() {
		Descriptor descriptor = descriptorRef.get();
		int size = descriptor.size;

		return descriptor.writeOperation.pending.get() ? --size : size;
	}
}
