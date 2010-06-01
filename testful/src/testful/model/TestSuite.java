/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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