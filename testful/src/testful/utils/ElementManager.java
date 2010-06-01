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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

/**
 * Manages a set of elements (&lt;T&gt;) with key (&lt;K&gt;). It allows one to
 * pick elements according to their key, easing certain type of operation.
 * 
 * @author matteo
 * @param <K> the type of the key
 * @param <T> the type of the element
 */
public class ElementManager<K, T extends ElementWithKey<K>> implements Iterable<T>, Cloneable, Serializable {

	private static final long serialVersionUID = -4011267075468446650L;

	private final Map<K, T> map;

	public ElementManager() {
		map = new HashMap<K, T>();
	}

	/**
	 * Create an element manager using values in the given map.<br>
	 * <b>Warning: the map will be captured and become state of the created
	 * instance. DO NOT MODIFY THE MAP AFTER CALLING THIS CONSTRUCTOR!</b>
	 * 
	 * @param map the map
	 */
	public ElementManager(Map<K, T> map) {
		this.map = map;
	}

	/**
	 * Create an element manager with the given elements
	 * 
	 * @param elems the element to put in the new element manager
	 */
	public ElementManager(T ... elems) {
		map = new HashMap<K, T>(elems.length);

		if(elems != null) 
			for(T elem : elems)
				put(elem);
	}

	/**
	 * Put an element in the element manager. If an element with the same key is
	 * present, this method simply returns false without modifying the element
	 * manager.
	 * 
	 * @param elem the element to put in the element manager
	 * @return true if the element is inserted, false is there is already an
	 *         element with the same key.
	 */
	public boolean put(T elem) {
		if(map.containsKey(elem.getKey())) return false;
		map.put(elem.getKey(), elem);
		return true;
	}

	/**
	 * Put an element in the element manager. If an element with the same key is
	 * already present, it is removed.
	 * 
	 * @param elem the element to put in the element manager
	 */
	public void putAndReplace(T elem) {
		map.put(elem.getKey(), elem);
	}

	/**
	 * Get the element with the given key
	 * 
	 * @param key the key of the element to get.
	 * @return an element
	 */
	public T get(K key) {
		return map.get(key);
	}

	/**
	 * Remove the element with the given key
	 * 
	 * @param key the key of the element to remove
	 * @return the removed element
	 */
	public T remove(K key) {
		return map.remove(key);
	}

	/**
	 * Iterate on all elements
	 */
	@Override
	public Iterator<T> iterator() {
		return map.values().iterator();
	}

	/**
	 * Check if the element manager is empty
	 * 
	 * @return true if it does not contains any element
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Returns the number of elements in this element manager
	 * 
	 * @return the number of elements
	 */
	public int size() {
		return map.size();
	}

	/**
	 * Clone this element manager
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ElementManager<K, T> clone() throws CloneNotSupportedException {
		ElementManager<K, T> ret = new ElementManager<K, T>();
		for(Entry<K, T> m : map.entrySet()) {
			ret.map.put(m.getKey(), (T)m.getValue().clone());
		}
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");

		for(Entry<K, T> e : map.entrySet())
			sb.append("(").append(e.getKey().toString()).append("=").append(e.getValue().toString()).append(")");

		return sb.append("}").toString();
	}

	/**
	 * Get an empty iterator (i.e. an iterator without any element).
	 * 
	 * @param <T> the type of the iterator
	 * @return an empty iterator
	 */
	public static <T> Iterator<T> getEmptyIterator() {
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public T next() {
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}
}
