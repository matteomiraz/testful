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

import testful.IConfigRunner;
import testful.TestFul;
import testful.utils.Cloner;
import testful.utils.ElementManager;
import testful.utils.ElementWithKey;

public class RunnerPool implements IRunner, ITestRepository {

	/** maximum number of jobs that the executor pool allows to queue */
	private static final int MAX_BUFFER = 25;

	private static RunnerPool singleton;
	public static RunnerPool getRunnerPool() {
		if(singleton == null)
			singleton = new RunnerPool();

		return singleton;
	}

	private static Logger logger = Logger.getLogger("testful.executor.worker");

	private boolean localWorkersStarted = false;

	/** manager for futures; it is safe in a multi-threaded environment */
	private final ElementManager<String, TestfulFuture<?>> futures;
	/** tests in queue */
	private final BlockingQueue<Context<?, ?>> tests;

	/** tests being evaluated */
	private final ConcurrentHashMap<String, Context<?, ?>> testsEval;

	private final String name;

	private RunnerPool() {
		tests = new ArrayBlockingQueue<Context<?, ?>>(MAX_BUFFER);
		name = "testful-" + TestFul.runId;

		futures = new ElementManager<String, TestfulFuture<?>>(new ConcurrentHashMap<String, TestfulFuture<?>>());
		testsEval = new ConcurrentHashMap<String, Context<?, ?>>();

		logger.fine("Created Runner Pool ");

		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
			registry.list();
			logger.finer("Found a RMI registry");
		} catch(Exception e) {
			try {
				registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
				logger.finer("Created a new RMI registry");
			} catch(RemoteException e1) {
				logger.log(Level.WARNING, "Distributed evaluation disabled", e);
				registry = null;
			}
		}

		Remote remote = null;
		if(registry != null) {
			try {
				remote = UnicastRemoteObject.exportObject(this, 0);
			} catch(RemoteException e) {
				logger.log(Level.WARNING, "Distributed evaluation disabled", e);
				remote = null;
			}
		}

		try {
			registry.bind(name, remote);
			logger.info("Registered executorPool at " + name);

		} catch(Exception e) {
			logger.log(Level.WARNING, "Remote evaluation disabled: ", e);

			registry = null;
		}

		if(registry == null || remote == null) startLocalWorkers();
	}

	public void config(IConfigRunner config) {
		if(config.isLocalEvaluation()) startLocalWorkers();

		if(config.getRemote() != null) {
			for (String remote : config.getRemote()) {
				addRemoteWorker(remote);
			}
		}
	}

	public void startLocalWorkers() {
		if (!localWorkersStarted) {
			try {
				WorkerManager wm = new WorkerManager(-1, 0);
				wm.addTestRepository(this);
				localWorkersStarted = true;
			} catch (RemoteException e) {
				// never happens: all it's done locally!
			}
		}
	}

	@Override
	public boolean addRemoteWorker(String rmiAddress) {
		if(rmiAddress == null) return false;

		try {
			IWorkerManager wm = (IWorkerManager) Naming.lookup(rmiAddress);
			wm.addTestRepository(this);
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
	public <T extends Serializable> Future<T> execute(Context<T, ? extends ExecutionManager<T>> ctx) {
		TestfulFuture<T> ret = new TestfulFuture<T>(ctx.id);
		futures.put(ret);

		try {
			tests.put(ctx);
		} catch(InterruptedException e) {
			// this should not happens
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public Context<?, ?> getTest() throws RemoteException {
		try {

			Context<?, ?> ret = tests.take();
			testsEval.put(ret.id, ret);

			return ret;

		} catch(InterruptedException e) {
			throw new RemoteException("Cannot take the test", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void putResult(String key, byte[] result) {

		testsEval.remove(key);
		TestfulFuture<Serializable> future = (TestfulFuture<Serializable>) futures.remove(key);

		if(future == null) logger.warning("Future with " + key + " not found");
		else future.setResult(Cloner.deserialize(result, true));
	}

	@Override
	public void putException(String key, byte[] exception) throws RemoteException {

		testsEval.remove(key);
		TestfulFuture<?> future = futures.remove(key);

		if(future == null) logger.warning("Future with " + key + " not found");
		else future.setException((Exception) Cloner.deserialize(exception, true));
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

		void setResult(T result) {
			if(completed) throw new Error("Future already completed!");

			synchronized(this) {
				this.result = result;
				this.completed = true;
				notifyAll();
			}
		}

		void setException(Exception exc) {
			if(completed) throw new Error("Future already completed!");

			synchronized(this) {
				this.exc = exc;
				this.completed = true;
				notifyAll();
			}
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			throw new Error("Cannot cancel tasks");
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
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			if(!completed)
				synchronized(this) {
					if(!completed) {
						if(unit != null && timeout >= 0) this.wait(unit.toMillis(timeout));
						else this.wait();
					}
				}

			if(!completed) throw new TimeoutException("Timeout expired!");

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
