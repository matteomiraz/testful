package testful.model;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 * This class stores tests, and is able to give back the best/worst test
 * stored based on its declared rating (during insertion).
 * This container is ideal for giving the best population to the evolutionary algorithm.
 * @author Tudor
 */
public class TestSuite {

	private static class Entry {
		final Operation[] test;
		final float quality;

		public Entry(Operation[] test, float quality) {
			this.test = test;
			this.quality = quality;
		}
	}

	private final Queue<Entry> tests = new PriorityQueue<Entry>(8, new Comparator<Entry>() {

		@Override
		public int compare(Entry o1, Entry o2) {
			if(o1.quality > o2.quality) return -1;
			if(o1.quality < o2.quality) return +1;

			return 0;
		}

	});

	public void add(TestCoverage test){
		tests.add(new Entry(test.getTest(), test.getRating()));
	}

	public Operation[] getBestTest(){
		Entry test = tests.poll();
		if(test != null) return test.test;
		else return null;
	}

	public void add(TestSuite tc) {
		tests.addAll(tc.tests);
	}
}