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
import testful.utils.Cloner;

/**
 * This class is able to set-up the evaluation context of the test (i.e. the
 * Execution Manager).<br/>
 * 
 * @author matteo
 * @param <R> the type of the <b>R</b>esult
 * @param <M> the Execution <b>M</b>anager
 */
public class Context<R extends Serializable, M extends ExecutionManager<R>> implements Serializable {

	private static final long serialVersionUID = 1615872139934821021L;

	private final static String ID_PREFIX = UUID.randomUUID().toString();
	private final static AtomicLong ID_SUFFIX = new AtomicLong(0);
	final String id;

	private final ClassFinder classFinder;
	private boolean recycleClassLoader = true;

	public boolean stopOnBug = true;
	private final String execManager;

	/** contains a compressed serialized array of Executor */
	private final byte[] executorSerGz;

	/** contains a compressed serialized array of TrackerDatum */
	private final byte[] trackerDataSerGz;

	/**
	 * Create a new evaluation context
	 * 
	 * @param execManager the execution manager to use
	 * @param cf the class finder
	 * @param executor the executor
	 * @param data the tracker data
	 */
	public Context(Class<M> execManager, ClassFinder cf, Executor executor, TrackerDatum ... data) {
		this(execManager, cf, Cloner.serializeWithCache(executor, true), Cloner.serializeWithCache(data, true));
	}
	
	public Context(Class<M> execManager, ClassFinder cf, byte[] executorSerGz, byte[] trackerDataSerGz) {
		this.id = ID_PREFIX + ":" + ID_SUFFIX.incrementAndGet();

		this.classFinder = cf;
		this.execManager = execManager.getCanonicalName();
		this.executorSerGz = executorSerGz;
		this.trackerDataSerGz = trackerDataSerGz;
	}

	public boolean isRecycleClassLoader() {
		return recycleClassLoader;
	}

	public void setRecycleClassLoader(boolean recycleClassLoader) {
		this.recycleClassLoader = recycleClassLoader;
	}

	public boolean isStopOnBug() {
		return stopOnBug;
	}

	public void setStopOnBug(boolean stopOnBug) {
		this.stopOnBug = stopOnBug;
	}

	public ClassFinder getClassFinder() {
		return classFinder;
	}

	public int getSize() {
		return executorSerGz.length + trackerDataSerGz.length;
	}
	
	@SuppressWarnings("unchecked")
	public ExecutionManager<R> getExecManager(TestfulClassLoader loader) throws ClassNotFoundException {
		try {
			Class<? extends ExecutionManager<R>> c = (Class<? extends ExecutionManager<R>>) loader.loadClass(execManager);
			Constructor<? extends ExecutionManager<R>> cns = c.getConstructor(new Class<?>[] { byte[].class, byte[].class, boolean.class});
			return cns.newInstance(executorSerGz, trackerDataSerGz, recycleClassLoader);
		} catch(Exception e) {
			throw new ClassNotFoundException("Cannot create the execution manager", e);
		}
	}
}
