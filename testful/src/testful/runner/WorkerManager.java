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

public class WorkerManager implements IWorkerManager, IJobRepository {

	private static Logger logger = Logger.getLogger("testful.executor.worker");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);

	private final Set<String> testRepositories = Collections.synchronizedSet(new HashSet<String>());

	private volatile boolean running = true;

	private final BlockingQueue<Job<?,?,?>> jobs;
	private final Map<String, IJobRepository> results;

	private final static int MAX_ELEMS = 20;
	private final static long MIN_AGE = 15 * 60 * 1000; // 15 min
	private final static long MIN_UNUSED = 5 * 60 * 1000; //  5 min

	private final CachingMap<String, DataFinder> finders;
	private final CachingMap<String, Queue<RemoteClassLoader>> classLoaders;

	private AtomicLong executedJobs = new AtomicLong();

	public WorkerManager(int cpu) {
		if(LOG_FINE) logger.fine("Starting: Worker Manager (" + TestFul.runId + ")");

		int buffer = TestFul.getProperty(TestFul.PROPERTY_RUNNER_WORKER_JOBS, 50);
		jobs = new ArrayBlockingQueue<Job<?,?,?>>(buffer);
		results = new ConcurrentHashMap<String, IJobRepository>();

		finders = new CachingMap<String, DataFinder>(MAX_ELEMS, MIN_AGE, MIN_UNUSED);
		classLoaders = new CachingMap<String, Queue<RemoteClassLoader>>(MAX_ELEMS, MIN_AGE, MIN_UNUSED);

		if(cpu > 0) {
			logger.info("Starting with " + cpu + " local executor threads");
		} else if (cpu == 0) {
			logger.info("Starting without CPUs: acting as a job repository.");
		} else { // cpu < 0
			cpu = Runtime.getRuntime().availableProcessors();
			logger.info("Detected " + cpu + " cpus (or cores): starting one thread per cpus.");
		}

		for(int i = 0; i < cpu; i++) createWorker();

		logger.info("Started " + cpu + " workers");
	}

	@Override
	public void addJobRepository(final IJobRepository rep) throws RemoteException {
		final String name = rep.getName();

		if(!testRepositories.add(name)) return;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String msg = "Added " + name;
					logger.info(msg);

					while(running) {
						Job<?,?,?> j = rep.getJob();
						logger.finest("Retrieved test: " + j.id);
						results.put(j.id, rep);
						jobs.put(j);
					}

				} catch(InterruptedException e) {
					String msg = "Interrupted: " + e.getMessage();
					logger.warning(msg);
					return;
				} catch(RemoteException e) {
					if(e.getCause() instanceof EOFException) {
						logger.info("Job Repository disconnected");
						return;
					} else {
						String msg = "Cannot contact job repository: " + e.getMessage();
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
	public void addJobRepository(String repName) throws RemoteException, MalformedURLException, NotBoundException {
		IJobRepository rep = (IJobRepository) Naming.lookup(repName);
		addJobRepository(rep);
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

		while(!jobs.isEmpty()) {
			logger.info("Waiting for " + jobs.size() + " jobs to execute...");
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
	@SuppressWarnings("unchecked")
	public <I extends Serializable, R extends Serializable> Job<I, R, ? extends IExecutor<I,R>> getJob() throws RemoteException {
		try {
			return (Job<I, R, ? extends IExecutor<I, R>>) jobs.take();
		} catch(InterruptedException e) {
			throw new RemoteException("interrupted", e);
		}
	}

	public RemoteClassLoader getClassLoader(Job<?,?,?> ctx) throws RemoteException {
		DataFinder finder = ctx.getFinder();
		String key = finder.getKey();

		RemoteClassLoader ret = null;
		if(!ctx.isReloadClasses()) {
			synchronized(classLoaders) {
				Cacheable<Queue<RemoteClassLoader>> q = classLoaders.get(key);
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

		ret = new RemoteClassLoader(cacheableFinder.getElement());

		return ret;
	}

	@Override
	public void putException(String key, Exception exc) throws RemoteException {
		try {
			IJobRepository rep = results.remove(key);
			rep.putException(key, exc);
		} catch(Exception e) {
			logger.log(Level.WARNING, "Cannot put the result back in the job repository: " + e.getMessage(), e);
		}

		executedJobs.incrementAndGet();
	}

	public void putException(Job<?,?,?> ctx, Exception exc, RemoteClassLoader cl) {
		if(cl != null)
			reuseClassLoader(cl);

		try {
			putException(ctx.id, exc);
		} catch(RemoteException e) {
			// never happens
		}
	}

	@Override
	public void putResult(String key, Serializable result) throws RemoteException {
		try {
			IJobRepository rep = results.remove(key);
			rep.putResult(key, result);
		} catch(Exception e) {
			logger.log(Level.WARNING, "Cannot put the result back in the job repository: " + e.getMessage(), e);
		}

		executedJobs.incrementAndGet();
	}

	public void putResult(Job<?,?,?> ctx, Serializable result, RemoteClassLoader cl) {
		reuseClassLoader(cl);

		try {
			putResult(ctx.id, result);
		} catch(RemoteException e) {
			// never happens
		}
	}

	private void reuseClassLoader(RemoteClassLoader cl) {
		synchronized(classLoaders) {
			Cacheable<Queue<RemoteClassLoader>> q = classLoaders.get(cl.getKey());

			if(q == null) {
				q = new Cacheable<Queue<RemoteClassLoader>>(new ArrayBlockingQueue<RemoteClassLoader>(10));
				classLoaders.put(cl.getKey(), q);
			}

			q.getElement().offer(cl);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Testful runner - ").append(new Date().toString());

		int waiting = jobs.size();
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

		sb.append("\n  mem: ").append(used/(1024*1024)).append("/").append(max/(1024*1024)).append(" Mb");

		return sb.toString();
	}
}
