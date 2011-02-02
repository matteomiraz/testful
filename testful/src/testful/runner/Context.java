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
import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import testful.coverage.TrackerDatum;
import testful.model.ExecutorSerializer;
import testful.model.Test;
import testful.utils.Cloner;
import testful.utils.StopWatch;

/**
 * This class is able to set-up the evaluation context of the test (i.e. the
 * Execution Manager).<br/>
 * @author matteo
 * @param <R> the type of the <b>R</b>esult
 * @param <M> the Execution <b>M</b>anager
 */
public class Context<R extends Serializable, M extends IExecutionManager<R>> implements Serializable {

	private static final long serialVersionUID = 1615872139934821021L;

	private static final StopWatch timer = StopWatch.getTimer();

	private final static String ID_PREFIX = UUID.randomUUID().toString();
	private final static AtomicLong ID_SUFFIX = new AtomicLong(0);
	final String id;

	private final DataFinder finder;
	private boolean reloadClasses = false;

	public boolean stopOnBug = true;
	private final String execManager;

	/** contains a compressed serialized array of Executor */
	private final byte[] executor;

	/** contains a compressed serialized array of TrackerDatum */
	private final byte[] trackerDataSerGz;

	/**
	 * Create a new evaluation context
	 * @param execManager the execution manager to use
	 * @param finder the data finder
	 * @param executorType the type of the executor to use
	 * @param test the test to execute
	 * @param data the tracker data
	 */
	public Context(Class<M> execManager, DataFinder finder, Class<? extends IExecutor> executorType, Test test, TrackerDatum ... data) {

		timer.start("exec.0.serialization");

		this.id = ID_PREFIX + ":" + ID_SUFFIX.incrementAndGet();

		this.finder = finder;
		this.execManager = execManager.getName();
		this.executor = ExecutorSerializer.serialize(finder, executorType, test);
		this.trackerDataSerGz = Cloner.serializeWithCache(data, true);

		timer.stop(Integer.toString(test.getTest().length));
	}

	public boolean isReloadClasses() {
		return reloadClasses;
	}

	/**
	 * Force the reload of all classes (i.e., does not enable the reuse of class loaders)
	 * @param reloadClasses true if the classes must be reloaded for each test execution
	 */
	public void setReloadClasses(boolean reloadClasses) {
		this.reloadClasses = reloadClasses;
	}

	public boolean isStopOnBug() {
		return stopOnBug;
	}

	public void setStopOnBug(boolean stopOnBug) {
		this.stopOnBug = stopOnBug;
	}

	public DataFinder getFinder() {
		return finder;
	}

	public int getSize() {
		return executor.length + trackerDataSerGz.length;
	}

	@SuppressWarnings("unchecked")
	public IExecutionManager<R> getExecManager(TestfulClassLoader loader) throws ClassNotFoundException {
		try {
			Class<? extends IExecutionManager<R>> c = (Class<? extends IExecutionManager<R>>) loader.loadClass(execManager);
			Constructor<? extends IExecutionManager<R>> cns = c.getConstructor(new Class<?>[] { byte[].class, byte[].class, boolean.class});
			return cns.newInstance(executor, trackerDataSerGz, reloadClasses);
		} catch(Exception e) {
			throw new ClassNotFoundException("Cannot create the execution manager", e);
		}
	}
}
