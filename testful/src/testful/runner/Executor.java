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

/**
 * Abstract executor.<br/>
 *
 * <b>This class must be loaded through the TestFul class loader.<b>
 * 
 * @author matteo
 * @param <I> The type of information required by the execution manager
 * @param <R> The result of the execution manager
 */
public abstract class Executor<I extends Serializable, R extends Serializable> implements IExecutor<I,R> {

	private static final Logger logger = Logger.getLogger("testful.runner");

	protected final TestfulClassLoader classLoader;
	/**
	 * Create a new execution manager.<br/>
	 * <b>NOTICE:</b> subclasses must have a default constructor
	 */
	public Executor() {
		if(Executor.class.getClassLoader() instanceof TestfulClassLoader) {
			classLoader = (TestfulClassLoader) Executor.class.getClassLoader();
		} else {
			classLoader = null;
			ClassCastException exc = new ClassCastException("The Executor must be loaded by a testful class loader");
			logger.log(Level.WARNING, exc.getMessage(), exc);
		}
	}

	private I input;
	private boolean reloadClasses;

	@Override
	public void setInput(I input) {
		this.input = input;
	}

	protected I getInput() {
		return input;
	}

	public void setReloadClasses(boolean reloadClasses) {
		this.reloadClasses = reloadClasses;
	}

	protected boolean isReloadClasses() {
		return reloadClasses;
	}

	@Override
	public abstract R execute() throws Exception;
}
