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
