/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
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
 * Interface to manage the execution of tests and collect some data.
 *
 * @author matteo
 * @param <T> The type of collected information
 */
public interface IExecutionManager<T extends Serializable> {

	/**
	 * Execute the test and retrieve the desired result.
	 * @param stopOnBug if true the execution will stop as soon as the first bug is revealed
	 * @return the desired result
	 * @throws ClassNotFoundException if it is impossible to find some classes
	 */
	T execute(boolean stopOnBug) throws ClassNotFoundException;

}
