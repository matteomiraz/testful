package testful.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
			System.err.println("WARN: CachingMap.put(key = null, value)");
			return value;
		}

		value.setCreationTimestamp(System.currentTimeMillis());
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends CachingMap.Cacheable<E>> m) {
		long now = System.currentTimeMillis();

		for(Entry<? extends K, ? extends Cacheable<E>> e : m.entrySet()) {
			if(e.getKey() == null) System.err.println("WARN: CachingMap.putAll() : an entry has a null key!");
			e.getValue().setCreationTimestamp(now);
		}

		super.putAll(m);
	}

	@Override
	public CachingMap.Cacheable<E> get(Object key) {
		if(key == null) {
			System.err.println("WARN: CachingMap.get(key = null)");
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
