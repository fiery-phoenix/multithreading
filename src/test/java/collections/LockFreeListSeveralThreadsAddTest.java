package collections;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LockFreeListSeveralThreadsAddTest {

	private static final int ITERATIONS = 100000;
	private static final int ADDERS = 10;

	@Test
	public void testCorrectness() {
		List<Integer> list = new LockFreeList<>();
		addValues(list);
		checkAddedValues(list);
	}

	private void checkAddedValues(List<Integer> list) {
		assertEquals(list.size(), ITERATIONS * ADDERS);
		int checkSum = 0;
		int listSum = 0;
		Map<Integer, List<Integer>> checkedAddedValues = createAddersValues();
		for (int i = 0; i < ITERATIONS; i++) {
			checkSum += i;
		}
		checkSum *= ADDERS;
		for (int i = 0; i < ITERATIONS * ADDERS; i++) {
			Integer value = list.get(i);
			listSum += value;
			boolean needsPlacement = true;
			// order check
			for (int j = 0; j < ADDERS && needsPlacement; j++) {
				if (tryAdd(checkedAddedValues.get(j), value)) {
					needsPlacement = false;
				}
			}
			if (needsPlacement) {
				fail("Couldn't add " + value + " with last = " + getAllLast(checkedAddedValues));
			}
		}

		assertEquals(checkSum, listSum);
	}

	private void addValues(List<Integer> list) {
		CountDownLatch latch = new CountDownLatch(1);
		ExecutorService executors = Executors.newFixedThreadPool(ADDERS);
		List<Future<?>> futures = new ArrayList<>();
		for (int i = 0; i < ADDERS; i++) {
			futures.add(executors.submit(new Adder(latch, list)));
		}
		latch.countDown();
		for (Future<?> future : futures) {
			try {
				future.get();
			}
			catch (InterruptedException | ExecutionException e) {
				fail(e.toString());
			}
		}
		executors.shutdown();
	}

	private Map<Integer, List<Integer>> createAddersValues() {
		Map<Integer, List<Integer>> addersValues = new HashMap<>();
		for (int i = 0; i < ADDERS; i++) {
			addersValues.put(i, new ArrayList<>());
		}

		return addersValues;
	}

	private String getAllLast(Map<Integer, List<Integer>> map) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ADDERS; i++) {
			builder.append(i).append(": ").append(getLast(map.get(i))).append("; ");
		}

		return builder.toString();
	}

	private Integer getLast(List<Integer> list) {
		return list.get(list.size() - 1);
	}

	private boolean tryAdd(List<Integer> values, Integer value) {
		if (values.isEmpty()) {
			if (value.equals(0)) {
				values.add(0);
				return true;
			} else {
				return false;
			}
		}

		Integer lastInList = values.get(values.size() - 1);
		if (lastInList.equals(value - 1)) {
			values.add(value);
			return true;
		}

		return false;
	}

	private static final class Adder implements Runnable {
		private final CountDownLatch latch;
		private final List<Integer> testing;

		private Adder(CountDownLatch latch, List<Integer> list) {
			this.latch = latch;
			this.testing = list;
		}

		@Override
		public void run() {
			try {
				latch.await();
				for (int i = 0; i < ITERATIONS; i++) {
					testing.add(i);
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
