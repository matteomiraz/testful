package testful.utils;

/**
 * This render an element with a key. This kind of elements are manageble with
 * the ElementManager Set.
 * 
 * @author matteo
 * @param <K> the type of the key
 */
public interface ElementWithKey<K> extends Cloneable {

	/**
	 * Returns the key of the element
	 * 
	 * @return the key
	 */
	public K getKey();
	
	public ElementWithKey<K> clone() throws CloneNotSupportedException;
}
