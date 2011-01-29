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

import testful.model.Operation;

/**
 * Interface for executing tests.
 * @author matteo
 */
public interface IExecutor {


	/**
	 * really execute the test, measuring the execution time.
	 * @param stopOnBug specify if the execution should stop at the first bug
	 *          revealed
	 * @return the number of faults
	 * @throws ClassNotFoundException if some class has not been found
	 */
	int execute(boolean stopOnBug) throws ClassNotFoundException;

	/**
	 * Returns the operations of the test.
	 * If this method is invoked after the execution of the test,
	 * each operation has the proper OperationInformation.
	 * @return the operations of the test.
	 */
	Operation[] getTest();

}
