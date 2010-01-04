package testful.runner;

import java.io.Serializable;
import java.lang.reflect.Method;

import testful.TestfulException;
import testful.coverage.TrackerDatum;
import testful.utils.Cloner;

/**
 * Embodies the execution environment of the test. Manages the supply of
 * information at run-time to trackers, the test execution, and the retrival of
 * coverage information.<br/>
 * The execution manager itself (i.e., this abstract class) is loaded by the system
 * classloader, but subclasses must be loaded using the test's class loader (instance
 * of the TestfulClassLoader). When one is creating an istance, he must specify the
 * executor to use and (optionally) the <b>tracker data</b>. Then it is possible
 * to execute the test and retrieve the results by invoking the method execute,
 * and specifying whether or not the exeuction must stop as soon as a bug is
 * revealed. This method performs the following operations:
 * <ol>
 * <li>invoke the setup method</li>
 * <li>if the classloader is a new one (never used before), invoke the warmup
 * method</li>
 * <li>invoke the reallyExecute() method, which really execute the test</li>
 * <li>invoke the getResult method, and return the result</li>
 * </ol>
 * 
 * @author matteo
 * @param <T> The type of the collected coverage information
 */
public abstract class ExecutionManager<T extends Serializable> {

	protected final boolean recycleClassLoader;

	protected final TestfulClassLoader classLoader;

	/** the executor of the test */
	protected final Executor executor;

	/** the execution time of the test (if null, the test has not been executed) */
	protected Long executionTime;

	/** the number of faults revealed */
	protected Integer faults;
	
	/**
	 * Create a new execution manager.<br/>
	 * <b>NOTICE:</b> subclasses must have the same constructor (same parameters.
	 * same order).
	 * @throws TestfulException if something really weird goes wrong
	 */
	public ExecutionManager(byte[] executorSerGz, byte[] trackerDataSerGz, boolean recycleClassloader) throws TestfulException {

		this.recycleClassLoader = recycleClassloader;
		
		ClassLoader loader = this.getClass().getClassLoader();
		if(!(loader instanceof TestfulClassLoader)) throw new ClassCastException("FATAL: The execution manager must be loaded using a testful class loader!");

		this.classLoader = (TestfulClassLoader) loader;

		try {
			Class<?> cloner = loader.loadClass(Cloner.class.getCanonicalName());
			Method deserialize = cloner.getMethod("deserialize", byte[].class, boolean.class);

			// Setup the executor
			if(executorSerGz != null)
				this.executor = (Executor) deserialize.invoke(null, executorSerGz, true);
			else
				this.executor = null;
			
			if(trackerDataSerGz != null) {
				// Setup the tracker data
				TrackerDatum[] data = (TrackerDatum[]) deserialize.invoke(null, trackerDataSerGz, true);
	
				Class<?> trackerDatum = loader.loadClass("testful.coverage.Tracker");
				Method setup = trackerDatum.getMethod("setup", TrackerDatum[].class);
				setup.invoke(null, new Object[] { data });
			}
		} catch(Throwable e) {
			throw new TestfulException("Cannot setup the execution manager", e);
		}
	}

	public Long getExecutionTime() {
		return executionTime;
	}

	/**
	 * Execute the test and retrieve the desired result. Internally, are performed
	 * the following operations:
	 * <ol>
	 * <li>invoke the setup method</li>
	 * <li>if the classloader is a new one (never used before), invoke the warmup
	 * method</li>
	 * <li>invoke the execute() method, which really execute the test</li>
	 * <li>invoke the getResult method, and return the result</li>
	 * </ol>
	 * 
	 * @param stopOnBug if true the execution will stop as soon as the first bug
	 *          is revealed
	 * @return the desired result
	 * @throws ClassNotFoundException if it is impossible to find some classes
	 */
	public T execute(boolean stopOnBug) throws ClassNotFoundException {
		this.executionTime = null;

		setup();

		if(!classLoader.isWarmedUp()) {
			warmUp();
			classLoader.setWarmedUp(true);
		}

		reallyExecute(stopOnBug);

		return getResult();
	}

	/**
	 * Prepare the execution of the test. This method is invoked by
	 * <code>execute</code> before running the test.
	 */
	protected abstract void setup() throws ClassNotFoundException;

	/**
	 * Warm up the class loader: perform some operation on the classes under test
	 * to pre-compile their bytecode and ensure faster executions.
	 */
	protected abstract void warmUp();

	/**
	 * Collect the results of the test execution
	 * 
	 * @return the collected coverage information
	 */
	protected abstract T getResult();

	/**
	 * really execute the test, measuring the execution time.
	 * 
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
