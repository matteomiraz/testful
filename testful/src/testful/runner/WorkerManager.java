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

import java.io.EOFException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.utils.CachingMap;
import testful.utils.CachingMap.Cacheable;
import testful.utils.Cloner;

public class WorkerManager implements IWorkerManager, ITestRepository {

	private static final boolean COMPRESS_SERIALIZED = TestFul.getProperty(TestFul.PROPERTY_COMPRESS_SERIALIZED, false);

	private static Logger logger = Logger.getLogger("testful.executor.worker");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);

	/** cache size: number of jobs to keep in local cache */
	private static final int CACHE_SIZE = 50;

	private final Set<String> testRepositories = Collections.synchronizedSet(new HashSet<String>());

	private volatile boolean running = true;

	private final BlockingQueue<Context<?, ?>> tests;
	private final Map<String, ITestRepository> results;

	private final static int MAX_ELEMS = 20;
	private final static long MIN_AGE = 15 * 60 * 1000; // 15 min
	private final static long MIN_UNUSED = 5 * 60 * 1000; //  5 min

	private final CachingMap<String, DataFinder> finders;
	private final CachingMap<String, Queue<TestfulClassLoader>> classLoaders;

	private AtomicLong executedJobs = new AtomicLong();

	private AtomicLong receivedBytes = new AtomicLong();
	private AtomicLong sentBytes = new AtomicLong();

	public WorkerManager(int cpu, int buffer) {
		if(LOG_FINE) logger.fine("Starting: Worker Manager (" + TestFul.runId + ")");

		if(buffer <= 0) buffer = CACHE_SIZE;
		tests = new ArrayBlockingQueue<Context<?, ?>>(buffer);
		results = new ConcurrentHashMap<String, ITestRepository>();

		finders = new CachingMap<String, DataFinder>(MAX_ELEMS, MIN_AGE, MIN_UNUSED);
		classLoaders = new CachingMap<String, Queue<TestfulClassLoader>>(MAX_ELEMS, MIN_AGE, MIN_UNUSED);

		if(cpu < 0) cpu = Runtime.getRuntime().availableProcessors();

		for(int i = 0; i < cpu; i++) createWorker();

		logger.info("Started " + cpu + " workers");
	}

	@Override
	public void addTestRepository(final ITestRepository rep) throws RemoteException {
		final String name = rep.getName();

		if(!testRepositories.add(name)) return;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String msg = "Added " + name;
					logger.info(msg);

					while(running) {
						Context<?, ?> t = rep.getTest();
						receivedBytes.addAndGet(t.getSize());
						logger.finest("Retrieved test: " + t.id);
						results.put(t.id, rep);
						tests.put(t);
					}

				} catch(InterruptedException e) {
					String msg = "Interrupted: " + e.getMessage();
					logger.warning(msg);
					return;
				} catch(RemoteException e) {
					if(e.getCause() instanceof EOFException) {
						logger.info("Test Repository disconnected");
						return;
					} else {
						String msg = "Cannot contact test repository: " + e.getMessage();
						logger.warning(msg);
						return;
					}
				}
			}
		});
		t.setName("WorkerManager-" + name);
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void addTestRepository(String repName) throws RemoteException, MalformedURLException, NotBoundException {
		ITestRepository rep = (ITestRepository) Naming.lookup(repName);
		addTestRepository(rep);
	}

	@Override
	public String getName() throws RemoteException {
		return "runner-" + TestFul.runId;
	}

	public void stop() {
		logger.info("Stopping all jobs");

		try {
			running = false;
			TimeUnit.SECONDS.sleep(5);
		} catch(InterruptedException e) {
		}

		while(!tests.isEmpty()) {
			logger.info("Waiting for " + tests.size() + " tests to execute...");
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch(InterruptedException e) {
			}
		}

		logger.info("Bye\n");
	}

	private void createWorker() {
		Worker w = new Worker(this);
		w.setDaemon(true);
		w.start();
	}

	@Override
	public Context<?, ?> getTest() throws RemoteException {
		try {
			return tests.take();
		} catch(InterruptedException e) {
			throw new RemoteException("interrupted", e);
		}
	}

	public TestfulClassLoader getClassLoader(Context<?, ?> ctx) throws RemoteException {
		DataFinder finder = ctx.getFinder();
		String key = finder.getKey();

		TestfulClassLoader ret = null;
		if(!ctx.isReloadClasses()) {
			synchronized(classLoaders) {
				Cacheable<Queue<TestfulClassLoader>> q = classLoaders.get(key);
				if(q != null) ret = q.getElement().poll();
			}
		}

		// if cacheable and cached
		if(ret != null) return ret;

		Cacheable<DataFinder> cacheableFinder;
		synchronized(finders) {
			cacheableFinder = finders.get(key);

			if(cacheableFinder == null) {
				if(finder instanceof DataFinderCaching) cacheableFinder = new Cacheable<DataFinder>(finder);
				else cacheableFinder = new Cacheable<DataFinder>(new DataFinderCaching(finder));

				finders.put(key, cacheableFinder);
			}
		}

		ret = new TestfulClassLoader(cacheableFinder.getElement());

		return ret;
	}

	@Override
	public void putException(String key, byte[] excSer, boolean compressed) throws RemoteException {
		try {
			ITestRepository rep = results.remove(key);
			rep.putException(key, excSer, compressed);
		} catch(Exception e) {
			logger.log(Level.WARNING, "Cannot put the result back in the test repository: " + e.getMessage(), e);
		}

		executedJobs.incrementAndGet();
		sentBytes.addAndGet(excSer.length);
	}

	public void putException(Context<?, ?> ctx, Exception exc, TestfulClassLoader cl) {
		if(cl != null)
			reuseClassLoader(cl);

		try {
			putException(ctx.id, Cloner.serialize(exc, COMPRESS_SERIALIZED), COMPRESS_SERIALIZED);
		} catch(RemoteException e) {
			// never happens
		}
	}

	@Override
	public void putResult(String key, byte[] resultSer, boolean compressed) throws RemoteException {
		try {
			ITestRepository rep = results.remove(key);
			rep.putResult(key, resultSer, compressed);
		} catch(Exception e) {
			logger.log(Level.WARNING, "Cannot put the result back in the test repository: " + e.getMessage(), e);
		}

		executedJobs.incrementAndGet();
		sentBytes.addAndGet(resultSer.length);
	}

	public void putResult(Context<?, ?> ctx, Serializable result, TestfulClassLoader cl) {
		reuseClassLoader(cl);

		try {
			putResult(ctx.id, Cloner.serialize(result, COMPRESS_SERIALIZED), COMPRESS_SERIALIZED);
		} catch(RemoteException e) {
			// never happens
		}
	}

	private void reuseClassLoader(TestfulClassLoader cl) {
		synchronized(classLoaders) {
			Cacheable<Queue<TestfulClassLoader>> q = classLoaders.get(cl.getKey());

			if(q == null) {
				q = new Cacheable<Queue<TestfulClassLoader>>(new ArrayBlockingQueue<TestfulClassLoader>(10));
				classLoaders.put(cl.getKey(), q);
			}

			q.getElement().offer(cl);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Testful runner - ").append(new Date().toString());

		int waiting = tests.size();
		int current = results.size();
		long done = executedJobs.get();

		sb.append("\n  jobs: ");
		sb.append(waiting).append(" waiting, ");
		sb.append(current - waiting).append(" running, ");
		sb.append(done).append(" completed.");

		long max = Runtime.getRuntime().maxMemory();
		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total-free;

		sb.append("\n  mem: ")
		.append(used/(1024*1024)).append("/")
		.append(max/(1024*1024)).append(" Mb");

		sb.append("; net: ").append(String.format("%.2f", receivedBytes.get()/(1024*1024.0))).append(" Mb in")
		.append(", ").append(String.format("%.2f", sentBytes.get()/(1024*1024.0))).append(" Mb out");

		return sb.toString();
	}
}
