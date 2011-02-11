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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is able to set-up the evaluation context of the test (i.e. the
 * Execution Manager).<br/>
 * @author matteo
 * @param <I> the type of the <b>I</b>nput
 * @param <R> the type of the <b>R</b>esult
 * @param <M> the Execution <b>M</b>anager
 */
public class Context<I extends Serializable, R extends Serializable, M extends IExecutor<I,R>> implements Serializable {

	private static Logger logger = Logger.getLogger("testful.executor");

	private static final long serialVersionUID = 1615872139934821021L;

	private final static String ID_PREFIX = UUID.randomUUID().toString();
	private final static AtomicLong ID_SUFFIX = new AtomicLong(0);
	final String id;

	private final DataFinder finder;

	/** True if the function must be executed in a new class loader */
	private boolean reloadClasses = false;

	/** The name of the execution manager to use */
	private final String execManager;

	/** The input */
	private final I input;

	/**
	 * Create a new evaluation context
	 * @param execManager the execution manager to use
	 * @param finder the data finder
	 * @param input the input of the function
	 */
	public Context(Class<M> execManager, DataFinder finder, I input) {

		this.id = ID_PREFIX + ":" + ID_SUFFIX.incrementAndGet();

		this.finder = finder;
		this.execManager = execManager.getName();
		this.input = input;
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

	public DataFinder getFinder() {
		return finder;
	}

	@SuppressWarnings("unchecked")
	public R execute(TestfulClassLoader loader) throws Exception {
		try {

			Class<? extends IExecutor<I,R>> executorClass = (Class<? extends IExecutor<I,R>>) loader.loadClass(execManager);
			IExecutor<I,R> executor = executorClass.newInstance();

			executor.setInput(input);
			R result = executor.execute();

			return result;
		} catch(Exception e) {
			logger.log(Level.FINER, "Exception in Context.execute: " + e.getMessage(), e);
			throw e;
		}
	}
}
