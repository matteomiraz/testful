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

import testful.TestFul;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.TestCluster;

/**
 * 
 * Classes implementing this interface must support their own serialization (and de-serialization) <b>in an efficient manner</b>.
 * 
 * Classes implementing this interface must not execute directly the test, but
 * must create another instance of another class, loaded using the specified
 * class loader, and use it to run the test.
 *
 * @author matteo
 */
public abstract class Executor implements IExecutor {

	/** Test cluster */
	protected final TestCluster cluster;

	/** types of elements in the repository */
	protected final Reference[] repositoryType;

	protected final Operation[] test;

	protected final boolean discoverFaults;

	/**
	 * Classes implementing this class MUST expose a constructor with EXACTLY these parameters.
	 * @param testCluster the test cluster
	 * @param testRefs the references used in the test
	 * @param testOps the sequence of operations
	 * @param faultDiscovery true if the fault discovery is enabled
	 */
	public Executor(TestCluster testCluster, Reference[] testRefs, Operation[] testOps, boolean faultDiscovery) {

		if(TestFul.DEBUG && !(Executor.class.getClassLoader() instanceof TestfulClassLoader))
			throw new ClassCastException("The executor must be loaded by the Testful Class Loader");

		cluster = testCluster;
		repositoryType = testRefs;
		test = testOps;
		discoverFaults = faultDiscovery;
	}

	/**
	 * Run the test, using the specified class loader.
	 * @param stopOnBug true if the execution must stop as soon as the first bug is revealed
	 * @return the number of bug found
	 * @throws ClassNotFoundException if there is any problem resolving classes
	 */
	@Override
	public abstract int execute(boolean stopOnBug) throws ClassNotFoundException;

	/**
	 * Returns the operations of the test.
	 * If this method is invoked after the execution of the test,
	 * each operation has the proper OperationInformation.
	 * @return the operations of the test.
	 */
	@Override
	public Operation[] getTest() {
		return test;
	}

	/**
	 * Returns the length of the test
	 * @return the length of the test
	 */
	public int getTestLength() {
		return test.length;
	}
}
