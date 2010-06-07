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


package testful.coverage;

import java.io.Serializable;

import testful.utils.ElementWithKey;

/**
 * This interface represents the coverage information. It is collected
 * dynamically by trackers during tests' execution.
 * 
 * @author matteo
 */
public interface CoverageInformation extends ElementWithKey<String>, Serializable, Cloneable {

	/**
	 * Return the coverage as a floating-point number. The higher, the better.
	 * 
	 * @return the coverage as a number
	 */
	public float getQuality();

	/**
	 * Returns the key of this invormation. It must be unique, since it is used by
	 * ElementWithKey
	 * 
	 * @return the key of this information
	 */
	@Override
	public String getKey();

	/**
	 * Returns the name of this information. It should be human-comprehensible.
	 * 
	 * @return the name of the collected coverage information
	 */
	public String getName();

	/**
	 * Modify this information by merging it with the given one. This information
	 * and the other one shares the same key, so they must be the same kind of
	 * information. If it is not so, think about choosing another key for your
	 * information.
	 * 
	 * @param other the information to merge.
	 */
	public void merge(CoverageInformation other);

	/**
	 * Check if this coverage information contains all the elements present in the
	 * other one.
	 * 
	 * @param other the other information to check
	 * @return true iff this contains all the elements present in other
	 */
	public boolean contains(CoverageInformation other);

	/**
	 * create an empty coverage information with the same type
	 * 
	 * @return returns an empty coverage information
	 */
	public CoverageInformation createEmpty();
	
	@Override
	public CoverageInformation clone() throws CloneNotSupportedException;
	
	@Override
	public String toString();
}
