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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import testful.TestFul;

public class CachingMap<K, E> extends LinkedHashMap<K, CachingMap.Cacheable<E>> {

	private static final long serialVersionUID = 3895919054962929993L;

	public static class Cacheable<E> {

		private final E element;

		public Cacheable(E element) {
			this.element = element;
		}

		public E getElement() {
			return element;
		}

		private long creationTimestamp;

		public long getCreationTimestamp() {
			return creationTimestamp;
		}

		public void setCreationTimestamp(long timestamp) {
			this.creationTimestamp = timestamp;
		}

		private long lastAccestTimestamp;

		public long getLastAccestTimestamp() {
			return lastAccestTimestamp;
		}

		public void setLastAccestTimestamp(long timestamp) {
			this.lastAccestTimestamp = timestamp;
		}
	}

	/** the map cannot contain more than maxElemns elems */
	private final int maxElems;

	/** removed elements must have at least minAge millisecs */
	private final long minAge;
	/** removed elements must be unused for at least minUnused millisecs */
	private final long minUnused;

	public CachingMap(int maxElems, long minAge, long minUnused) {
		super(16, 0.75f, true);
		this.maxElems = maxElems;
		this.minAge = minAge;
		this.minUnused = minUnused;
	}

	@Override
	public CachingMap.Cacheable<E> put(K key, CachingMap.Cacheable<E> value) {
		if(key == null) {
			if(TestFul.DEBUG) throw new NullPointerException("Cannot insert an element with null key in the CachingMap");
			else  return value;
		}

		value.setCreationTimestamp(System.currentTimeMillis());
		return super.put(key, value);
	}

	@Override
	@SuppressWarnings("unused")
	public void putAll(Map<? extends K, ? extends CachingMap.Cacheable<E>> m) {
		long now = System.currentTimeMillis();

		for(Entry<? extends K, ? extends Cacheable<E>> e : m.entrySet()) {
			if(TestFul.DEBUG && e.getKey() == null) throw new NullPointerException("Cannot insert an element with null key in the CachingMap");
			e.getValue().setCreationTimestamp(now);
		}

		super.putAll(m);
	}

	@Override
	public CachingMap.Cacheable<E> get(Object key) {
		if(key == null) {
			if(TestFul.DEBUG) throw new NullPointerException("Cannot look for an element providing a null key with the CachingMap");
			return null;
		}

		CachingMap.Cacheable<E> e = super.get(key);
		if(e != null) e.setLastAccestTimestamp(System.currentTimeMillis());

		return e;
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, CachingMap.Cacheable<E>> eldest) {

		if(size() > maxElems)
			if(removeExpired()) return false;
			else {
				return true;
			}

		final long now = System.currentTimeMillis();

		final CachingMap.Cacheable<E> value = eldest.getValue();
		final long age = now - value.getCreationTimestamp();
		final long unused = now - value.getLastAccestTimestamp();

		return age > minAge && unused > minUnused;
	}

	public boolean removeExpired() {
		Iterator<CachingMap.Cacheable<E>> valueIterator = values().iterator();

		final long now = System.currentTimeMillis();

		boolean removedSomething = false;
		while(valueIterator.hasNext()) {
			CachingMap.Cacheable<E> value = valueIterator.next();

			final long age = now - value.getCreationTimestamp();
			final long unused = now - value.getLastAccestTimestamp();

			if(age > minAge && unused > minUnused) {
				removedSomething = true;
				valueIterator.remove();
			}
		}

		return removedSomething;
	}

	public int getMaxCapacity() {
		return maxElems;
	}

	@Override
	public String toString() {
		return Integer.toString(size());
	}
}
