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
 * TrackerDatum represents a quantum of data usable by trackers.<br/>
 * Instances of this class should be given to the execution manager, which will
 * setup properly the execution environment, making available all the tracker
 * data.<br/>
 * Trackers can retrive the information they need by using the Tracker's static
 * method <code>getDatum</code> and specifying the key of the information.
 * 
 * @author matteo
 */
public interface TrackerDatum extends ElementWithKey<String>, Serializable {

	/**
	 * Each Tracker Datum must have a unique key.
	 */
	@Override
	public abstract String getKey();
}
