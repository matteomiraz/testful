package testful.utils;

import java.io.Serializable;
import java.util.Map.Entry;

public class SimpleEntry<K, V> implements Entry<K, V>, Serializable {

	private static final long serialVersionUID = -1142998188339397105L;

	private final K key;
	private V value;

	public SimpleEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		return(this.value = value);
	}

}
