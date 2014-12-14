package collections;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LockFreeListOneThreadTest {

	List<Integer> list = new LockFreeList<>();

	@Test
	public void testAddOne() {
		list.add(4);

		assertEquals(list.size(), 1);
		assertEquals(Integer.valueOf(4), list.get(0));
	}

	@Test
	public void testAddSeveral() {
		int size = 100;
		for (int i = 0; i < size; i++) {
			list.add(i);
		}

		assertEquals(list.size(), 100);
		for (int i = 0; i < size; i++) {
			assertEquals(Integer.valueOf(i), list.get(i));
		}
	}

	@Test
	public void testSet() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			list.add(i);
		}

		list.set(2, 5);
		assertEquals(list.size(), 5);
		assertEquals(Integer.valueOf(5), list.get(2));
		for (int i = 0; i < size; i++) {
			if (i == 2) {
				i++;
			}
			assertEquals(Integer.valueOf(i), list.get(i));
		}
	}

}