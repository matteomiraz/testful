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
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.TestfulException;
import testful.coverage.TrackerDatum;
import testful.model.ExecutorSerializer;
import testful.utils.Cloner;
import testful.utils.Timer;

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
 * @param <T> The type of the collected coverage information
 */
public abstract class ExecutionManager<T extends Serializable> implements IExecutionManager<T> {

	private static final Logger logger = Logger.getLogger("testful.runner");

	private static final Timer timer = Timer.getTimer();

	protected final boolean reloadClasses;

	/** the executor of the test */
	protected final IExecutor executor;

	/** the execution time of the test (if null, the test has not been executed) */
	protected Long executionTime;

	/** the number of faults revealed */
	protected Integer faults;

	/**
	 * Create a new execution manager.<br/>
	 * <b>NOTICE:</b> subclasses must have the same constructor (same parameters. same order).
	 * @throws TestfulException if something really weird goes wrong
	 */
	public ExecutionManager(byte[] executor, byte[] trackerDataSerGz, boolean reloadClasses) throws TestfulException {

		timer.start("exec.0.deserialization");

		this.reloadClasses = reloadClasses;

		if(TestFul.DEBUG && !(ExecutionManager.class.getClassLoader() instanceof TestfulClassLoader)) {
			ClassCastException exc = new ClassCastException("The execution manager must be loaded by using a testful class loader!");
			logger.log(Level.WARNING, exc.getMessage(), exc);
		}

		// Setup the executor
		if(executor != null)
			this.executor = ExecutorSerializer.deserialize(executor);
		else
			this.executor = null;

		if(trackerDataSerGz != null) {
			// Setup the tracker data
			TrackerDatum[] data = (TrackerDatum[]) Cloner.deserialize(trackerDataSerGz, true);

			testful.coverage.Tracker.setup(data);
		}

		timer.stop(Integer.toString(this.executor.getTest().length));
	}

	/**
	 * Create a new execution manager.<br/>
	 * <b>WARNING</b>: this method should be used only by other Execution Managers, and the class must be loaded through the testful class loader
	 * @throws TestfulException if something really weird goes wrong
	 */
	public ExecutionManager(IExecutor executor, TrackerDatum[] data, boolean reloadClasses) throws TestfulException {

		this.reloadClasses = reloadClasses;

		if(TestFul.DEBUG && !(ExecutionManager.class.getClassLoader() instanceof TestfulClassLoader)) {
			ClassCastException exc = new ClassCastException("The execution manager must be loaded using a testful class loader!");
			logger.log(Level.WARNING, exc.getMessage(), exc);
		}

		this.executor = executor;

		testful.coverage.Tracker.setup(data);
	}

	public Long getExecutionTime() {
		return executionTime;
	}

	/**
	 * Execute the test and retrieve the desired result. Internally, are performed
	 * the following operations:
	 * <ol>
	 * <li>invoke the setup method</li>
	 * <li>invoke the execute() method, which really execute the test</li>
	 * <li>invoke the getResult method, and return the result</li>
	 * </ol>
	 * @param stopOnBug if true the execution will stop as soon as the first bug
	 *          is revealed
	 * @return the desired result
	 * @throws ClassNotFoundException if it is impossible to find some classes
	 */
	@Override
	public T execute(boolean stopOnBug) throws ClassNotFoundException {
		this.executionTime = null;

		setup();

		reallyExecute(stopOnBug);

		return getResult();
	}

	/**
	 * Prepare the execution of the test. This method is invoked by
	 * <code>execute</code> before running the test.
	 */
	protected abstract void setup() throws ClassNotFoundException;

	/**
	 * Collect the results of the test execution
	 * @return the collected coverage information
	 */
	protected abstract T getResult();

	/**
	 * really execute the test, measuring the execution time.
	 * @param stopOnBug specify if the execution should stop at the first bug
	 *          revealed
	 * @throws ClassNotFoundException if some class has not been found
	 */
	protected void reallyExecute(boolean stopOnBug) throws ClassNotFoundException {
		long start = System.currentTimeMillis();
		faults = executor.execute(stopOnBug);
		long stop = System.currentTimeMillis();

		this.executionTime = (stop - start);
	}
}
