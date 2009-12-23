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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import testful.utils.Cloner;
import testful.utils.ElementManager;
import testful.utils.ElementWithKey;
import testful.utils.TestfulLogger;

public class RunnerPool implements IRunner, ITestRepository {

	/** maximum number of jobs that the executor pool allows to queue */
	private static final int MAX_BUFFER = 25;

	public static final String RMI_NAME = "pool";

	private static Logger logger = Logger.getLogger("testful.executor.worker");

	/**
	 * Create a test executor
	 * 
	 * @param noLocal if true do not allow a local evaluation of tests
	 * @param bufferSize the size of the buffer ( if <=0, the buffer has no
	 *          limits)
	 * @return the test executor
	 */
	public static IRunner createExecutor(String moduleName, boolean noLocal, int bufferSize) {
		RunnerPool executor = new RunnerPool(moduleName, bufferSize);

		logger.info("Created Runner Pool ");

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
				String msg = "Distributed evaluation disabled: " + e1;
				logger.warning(msg);
				System.err.println(msg);
				registry = null;
			}
		}

		Remote remote = null;
		if(registry != null) try {
			remote = UnicastRemoteObject.exportObject(executor, 0);
		} catch(RemoteException e) {
			String msg = "Distributed evaluation disabled: " + e;
			logger.warning(msg);
			System.err.println(msg);
			remote = null;
		}

		if(moduleName != null) {
			try {
				String remoteName = RMI_NAME + "-" + moduleName +  "-" + TestfulLogger.singleton.runId;
				registry.bind(remoteName, remote);
				String msg = "Registered executorPool at " + remoteName;
				logger.info(msg);
				System.out.println(msg);

			} catch(Exception e) {
				String msg = "Remote evaluation disabled: " + e;
				logger.warning(msg);
				System.err.println(msg);

				registry = null;
			}
		}
	
		if(!noLocal || registry == null || remote == null) WorkerManager.createLocalWorkers(executor);

		return executor;
	}

	private static IRunner executors;
	public static IRunner createExecutor(String moduleName, boolean noLocal) {
		if(executors == null)
			executors = createExecutor(moduleName, noLocal, MAX_BUFFER);

		return executors;
	}

	@Override
	public boolean addRemoteWorker(String rmiAddress) {
		if(rmiAddress == null) return false;
		
		try {
			IWorkerManager wm = (IWorkerManager) Naming.lookup(rmiAddress);
			wm.addTestRepository(this);
			return true;
		} catch(MalformedURLException e) {
			String msg = "Invalid RMI address: " + e.getMessage();
			System.err.println(msg);
			logger.warning(msg);
		} catch(RemoteException e) {
			String msg = "Error during the remote invocation: " + e.getMessage();
			System.err.println(msg);
			logger.warning(msg);
		} catch(NotBoundException e) {
			String msg = "The RMI address is not bound: " + e.getMessage();
			System.err.println(msg);
			logger.warning(msg);
		}
		return false;
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

	/** manager for futures; it is safe in a multi-threaded environment */
	private final ElementManager<String, TestfulFuture<?>> futures;
	/** tests in queue */
	private final BlockingQueue<Context<?, ?>> tests;

	/** tests being evaluated */
	private final ConcurrentHashMap<String, Context<?, ?>> testsEval;

	private final String name;
	
	private RunnerPool(String name, int bufferSize) {
		if(bufferSize > 0) tests = new ArrayBlockingQueue<Context<?, ?>>(bufferSize);
		else tests = new LinkedBlockingQueue<Context<?, ?>>();

		futures = new ElementManager<String, TestfulFuture<?>>(new ConcurrentHashMap<String, TestfulFuture<?>>());
		testsEval = new ConcurrentHashMap<String, Context<?, ?>>();
		
		this.name = name;
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

		if(future == null) System.err.println("ERROR: " + key + " future does not exist!");
		else future.setResult(Cloner.deserialize(result, true));
	}

	@Override
	public void putException(String key, byte[] exception) throws RemoteException {

		testsEval.remove(key);
		TestfulFuture<?> future = futures.remove(key);

		if(future == null) System.err.println("ERROR: " + key + " future does not exist!");
		else future.setException((Exception) Cloner.deserialize(exception, true));
	}
}
