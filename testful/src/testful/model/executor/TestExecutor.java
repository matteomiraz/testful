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

package testful.model.executor;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.coverage.Tracker;
import testful.model.Test;
import testful.runner.Executor;

/**
 * Abstract manager for executing tests.<br/>
 * It supplies run-time information to trackers, execute the test, and retrieve the information.<br/>
 * 
 * <b>This class is loaded through the Testful ClassLoader.<b>
 * 
 * To create an instance, you must specify the executor to use and (optionally) the <b>tracker data</b>.
 * Then it is possible to execute the test and retrieve the results by invoking the method execute,
 * and specifying whether or not the execution must stop as soon as a bug is
 * revealed. This method performs the following operations:
 * <ol>
 * <li>invoke the setup method</li>
 * <li>invoke the reallyExecute() method, which really execute the test</li>
 * <li>invoke the getResult method, and return the result</li>
 * </ol>
 *
 * @author matteo
 * @param <R> The type of the collected coverage information
 */
public abstract class TestExecutor<R extends Serializable> extends Executor<TestExecutorInput,R> {

	private static final Logger logger = Logger.getLogger("testful.model.executor");

	public TestExecutor() {
		super();
	}

	protected Integer faults = null;
	protected Long executionTime = null;

	/**
	 * Execute the test and retrieve the desired result. Internally, are performed
	 * the following operations:
	 * <ol>
	 * <li>invoke the setup method</li>
	 * <li>invoke the execute() method, which really execute the test</li>
	 * <li>invoke the getResult method, and return the result</li>
	 * </ol>
	 * @return the desired result
	 * @throws Exception in case of any error
	 */
	@Override
	public R execute() throws Exception {
		try {
			TestExecutorInput input = getInput();

			Tracker.setup(input.getTrackerData());

			setup();

			boolean discoverFaults = input.isDiscoverFaults();
			boolean stopOnBug = input.isStopOnBug();
			Test test = input.getTest();

			long start = System.currentTimeMillis();
			faults = ReflectionExecutor.execute(test, discoverFaults, stopOnBug);
			long stop = System.currentTimeMillis();

			executionTime = (stop - start);

			return getResult();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Prepare the execution of the test. This method is invoked by
	 * <code>execute</code> before running the test.
	 */
	protected abstract void setup() throws Exception;

	/**
	 * Collect the results of the test execution
	 * @return the collected coverage information
	 */
	protected abstract R getResult();
}
