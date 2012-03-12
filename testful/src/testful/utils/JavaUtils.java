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

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class JavaUtils {

	private static final Logger logger = Logger.getLogger("testful.utils");

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

	public static <T> int getIndex(Iterable<T> it, T elem) {
		return getIndex(it.iterator(), elem);
	}

	public static <T> int getIndex(Iterator<T> it, T elem) {
		int i = 0;
		while(it.hasNext()) {
			if(it.next() == elem) return i;
			i++;
		}
		return -1;
	}

	public static <T> int getIndex(T[] array, T elem) {
		for(int i = 0; i < array.length; i++)
			if(array[i] == elem) return i;

		return -1;
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

	public static File[] merge(File a, File b, List<File> list) {
		File[] ret = new File[(a!=null?1:0) + (b!=null?1:0) + list.size()];

		int i = 0;
		if(a != null) ret[i++] = a;
		if(b != null) ret[i++] = b;
		for (File f : list) ret[i++] = f;

		return ret ;
	}

	public static boolean isPublic(Class<?> c) {
		int mod = c.getModifiers();
		if(!Modifier.isPublic(mod)) return false;
		if(c.isAnonymousClass()) return false;

		Class<?> enclosing = c.getEnclosingClass();
		if(enclosing != null && !isPublic(enclosing)) return false;

		return true;
	}

	public static String escape(Object value) {
		if(value == null)
			return "null";

		if(value instanceof Boolean)
			return value.toString();

		if(value instanceof Byte)
			return value.toString();

		if(value instanceof Short)
			return value.toString();

		if(value instanceof Integer)
			return value.toString();

		if(value instanceof Long)
			return value.toString() + "l";

		if(value instanceof Float)
			return JavaUtils.escape((Float) value);

		if(value instanceof Double)
			return JavaUtils.escape((Double) value);

		if(value instanceof Character)
			return "'" + JavaUtils.escape(((Character) value).charValue()) + "'";

		if(value instanceof String)
			return JavaUtils.escape((String)value);

		logger.warning("Cannot escape " + value.getClass().getName());
		return value.toString();
	}

	public static String escape(final char orig)  {
		if(orig == '\b') return "\\b";	// \b
		if(orig == '\t') return "\\t";	// \t
		if(orig == '\n') return "\\n";	// \n
		if(orig == '\f') return "\\f";	// \f
		if(orig == '\r') return "\\r";	// \r
		if(orig == '\"') return "\\\"";	// "
		if(orig == '\'') return "\\\'";	// '
		if(orig == '\\') return "\\\\";	// \
		if(orig >= 32 && orig <= 126) return Character.toString(orig);
		if (orig > 0xffff) return "\\u" + Integer.toHexString(orig).toUpperCase(Locale.ENGLISH);
		if (orig > 0xfff) return "\\u" + Integer.toHexString(orig).toUpperCase(Locale.ENGLISH);
		if (orig > 0xff) return "\\u0" + Integer.toHexString(orig).toUpperCase(Locale.ENGLISH);
		if (orig > 0xf) return "\\u00" + Integer.toHexString(orig).toUpperCase(Locale.ENGLISH);
		return "\\u000" + Integer.toHexString(orig).toUpperCase(Locale.ENGLISH);
	}

	public static String escape(final String orig) {
		StringBuilder escaped = new StringBuilder();
		for (int i = 0; i < orig.length(); i++)
			escaped.append(escape(orig.charAt(i)));

		return "\"" + escaped.toString() + "\"";
	}

	public static String escape(final Double orig) {
		if(orig.isNaN()) return "Double.NaN";
		if(orig.isInfinite()) {
			if(orig.doubleValue() > 0) return "Double.POSITIVE_INFINITY";
			else return "Double.NEGATIVE_INFINITY";
		}

		return orig.toString();
	}

	public static String escape(final Float orig) {
		if(orig.isNaN()) return "Float.NaN";
		if(orig.isInfinite()) {
			if(orig.doubleValue() > 0) return "Float.POSITIVE_INFINITY";
			else return "Float.NEGATIVE_INFINITY";
		}

		return orig.toString();
	}
}
