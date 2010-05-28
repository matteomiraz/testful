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


package testful.runner;

import java.io.Serializable;

/**
 * Interface for the test executor.<br/>
 * Classes implementing this interface must not execute directly the test, but
 * must create another instance of another class, loaded using the specified
 * class loader, and use it to run the test.
 * 
 * @author matteo
 */
public interface Executor extends Serializable {

	/**
	 * Run the test, using the specified class loader.
	 * @param stopOnBug true if the execution must stop as soon as the first bug
	 *          is revealed
	 * 
	 * @return the number of bug found
	 * @throws ClassNotFoundException if there is any problem resolving classes
	 */
	public int execute(boolean stopOnBug) throws ClassNotFoundException;

	/**
	 * Returns the length of the test
	 * @return the length of the test
	 */
	public int getTestLength();
}
