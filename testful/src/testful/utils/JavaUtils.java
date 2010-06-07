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


package testful.utils;

import java.util.BitSet;

public class JavaUtils {

	/**
	 * Check if the two arrays contains the same elements, in any order.<br>
	 * For example {1, 2, 1} is similar to {2, 1, 1}, but is not similar to {2, 1,
	 * 2}.
	 * 
	 * @param <T> the base type
	 * @param a the first array
	 * @param b the second array
	 * @return true if two array are similar
	 */
	public static <T> boolean similar(T[] a, T[] b) {
		if(a == b) return true;
		if(a == null || b == null) return false;
		if(a.length != b.length) return false;

		boolean considered[] = new boolean[a.length];
		for(int i = 0; i < a.length; i++)
			if(!considered[i]) {
				int[] idxA = getIndexes(a, a[i]);
				int[] idxB = getIndexes(b, a[i]);

				if(idxA.length != idxB.length) return false;

				for(int j : idxA)
					considered[j] = true;
			}

		return true;

	}

	public static <T> int[] getIndexes(T[] array, T elem) {
		BitSet set = new BitSet(array.length);

		for(int i = 0; i < array.length; i++)
			if(elem == array[i] || (elem != null && elem.equals(array[i]))) set.set(i);

		int j = 0;
		int[] ret = new int[set.cardinality()];
		for(int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1))
			ret[j++] = i;

		return ret;
	}
}
