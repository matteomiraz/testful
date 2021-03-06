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
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.utils.ElementManager;
import testful.utils.ElementWithKey;

public class RunnerPool implements IRunner, IJobRepository {

	private static RunnerPool singleton;
	public static RunnerPool getRunnerPool() {
		if(singleton == null)
			singleton = new RunnerPool();

		return singleton;
	}

	private static Logger logger = Logger.getLogger("testful.executor.worker");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);
	private static final boolean LOG_FINER = logger.isLoggable(Level.FINER);

	/** manager for futures; it is safe in a multi-threaded environment */
	private final ElementManager<String, TestfulFuture<?>> futures;

	/** jobs in queue */
	private final BlockingQueue<Job<?,?,?>> jobs;

	/** jobs being evaluated */
	private final ConcurrentHashMap<String, Job<?,?,?>> jobsEval;

	private final String name;

	private RunnerPool() {
		int testBuffer = TestFul.getProperty(TestFul.PROPERTY_RUNNER_TESTREPOSITORY_JOBS, 1000);
		jobs = new ArrayBlockingQueue<Job<?,?,?>>(testBuffer);
		name = "testful-" + TestFul.runId;

		futures = new ElementManager<String, TestfulFuture<?>>(new ConcurrentHashMap<String, TestfulFuture<?>>());
		jobsEval = new ConcurrentHashMap<String, Job<?,?,?>>();

		if(LOG_FINE) logger.fine("Created Runner Pool ");

		int localWorkers = TestFul.getProperty(TestFul.PROPERTY_RUNNER_LOCAL_WORKERS, -1);
		if(localWorkers != 0) {
			try {
				WorkerManager wm = new WorkerManager(localWorkers);
				wm.addJobRepository(this);
			} catch (RemoteException e) {
				// never happens: it's done locally!
			}
		}

		if(TestFul.getProperty(TestFul.PROPERTY_RUNNER_REMOTE, false)) {
			Registry registry = null;
			try {
				registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
				registry.list();
				if(LOG_FINER) logger.finer("Found a RMI registry");
			} catch(Exception e) {
				try {
					registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
					if(LOG_FINER) logger.finer("Created a new RMI registry");
				} catch(RemoteException e1) {
					logger.log(Level.WARNING, "Distributed evaluation disabled", e);
				}
			}

			if(registry != null) {
				Remote remote = null;
				try {
					remote = UnicastRemoteObject.exportObject(this, 0);
				} catch(RemoteException e) {
					logger.log(Level.WARNING, "Distributed evaluation disabled", e);
					remote = null;
				}

				if (remote != null) {
					try {
						registry.bind(name, remote);
						logger.info("Registered executorPool at " + name);

					} catch (Exception e) {
						logger.log(Level.WARNING, "Remote evaluation disabled: ", e);
						registry = null;
					}
				}
			}

			String remoteProp = TestFul.getProperty(TestFul.PROPERTY_RUNNER_REMOTE_ADDR, "");
			String[] remotes = remoteProp.split(",");
			for (String remote : remotes) {
				remote = remote.trim();
				if(!remote.isEmpty())
					addRemoteWorker(remote);
			}

		} else { // warn the user if he specifies any remote option

			String remoteRunnersAddr = TestFul.getProperty(TestFul.PROPERTY_RUNNER_REMOTE_ADDR, "");
			if(!remoteRunnersAddr.isEmpty()) {
				logger.warning("Remote evaluation is disabled. Ignoring remote workers " + remoteRunnersAddr);
			}

		}
	}

	@Override
	public boolean addRemoteWorker(String rmiAddress) {
		if(rmiAddress == null) return false;

		try {
			IWorkerManager wm = (IWorkerManager) Naming.lookup(rmiAddress);
			wm.addJobRepository(this);
			return true;
		} catch(MalformedURLException e) {
			logger.log(Level.WARNING, "Invalid RMI address", e);
		} catch(RemoteException e) {
			logger.log(Level.WARNING, "Error during the remote invocation", e);
		} catch(NotBoundException e) {
			logger.log(Level.WARNING, "The RMI address is not bound", e);
		}
		return false;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public <I extends Serializable, R extends Serializable> Future<R> execute(Job<I, R, ? extends IExecutor<I,R>> ctx) {
		TestfulFuture<R> ret = new TestfulFuture<R>(ctx.id);
		futures.put(ret);

		try {
			jobs.put(ctx);
		} catch(InterruptedException e) {
			// this should not happens
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		return ret;
	}

	@Override
	public <I extends Serializable, R extends Serializable> Job<I, R, ? extends IExecutor<I,R>> getJob() throws RemoteException {
		try {

			@SuppressWarnings("unchecked")
			Job<I, R, ? extends IExecutor<I, R>> ret = (Job<I, R, ? extends IExecutor<I, R>>) jobs.take();
			jobsEval.put(ret.id, ret);

			return ret;

		} catch(InterruptedException e) {
			throw new RemoteException("Cannot take the test", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void putResult(String key, Serializable result) {

		jobsEval.remove(key);
		TestfulFuture<Serializable> future = (TestfulFuture<Serializable>) futures.remove(key);

		if(future == null) logger.warning("Future with " + key + " not found");
		else future.setResult(result);
	}

	@Override
	public void putException(String key, Exception exc) throws RemoteException {

		jobsEval.remove(key);
		TestfulFuture<?> future = futures.remove(key);

		if(future == null) logger.warning("Future with " + key + " not found");
		else future.setException(exc);
	}

	private static class TestfulFuture<T extends Serializable> implements Future<T>, ElementWithKey<String> {

		private final String key;
		private boolean completed = false;
		private T result = null;
		private Exception exc = null;

		public TestfulFuture(String key) {
			this.key = key;
		}

		@Override
		public String getKey() {
			return key;
		}

		synchronized void setResult(T result) {

			if(TestFul.DEBUG) {
				if(completed) TestFul.debug(new IllegalStateException("Future already completed!"));
				if(result == null) TestFul.debug(new Exception("The result cannot be null"));
			}

			if(completed) throw new IllegalStateException("Future already completed!");

			this.result = result;
			this.completed = true;
			notifyAll();
		}

		synchronized void setException(Exception exc) {

			if(TestFul.DEBUG) {
				if(completed) TestFul.debug(new IllegalStateException("Future already completed!"));
				if(exc == null) TestFul.debug(new Exception("The exception cannot be null"));
			}

			if(completed) throw new IllegalStateException("Future already completed!");

			this.exc = exc;
			this.completed = true;
			notifyAll();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			throw new UnsupportedOperationException("Cannot cancel tasks");
		}

		@Override
		public T get() throws InterruptedException, ExecutionException {
			try {
				return get(-1, null);
			} catch(TimeoutException e) {
				throw new InterruptedException(e.getMessage());
			}
		}

		@Override
		public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			if(!completed) {
				if(unit != null && timeout >= 0) this.wait(unit.toMillis(timeout));
				else this.wait();
			}

			if(!completed) throw new TimeoutException("Timeout expired!");

			if(TestFul.DEBUG) {
				if(result == null && exc == null)
					TestFul.debug(new Exception("A completed task must have the result or the exception set."));
			}

			if(result != null) return result;

			throw new ExecutionException(exc);
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return completed;
		}

		@Override
		public TestfulFuture<T> clone() throws CloneNotSupportedException {
			throw new CloneNotSupportedException("Clone not supported in TestfulFuture");
		}
	}
}
