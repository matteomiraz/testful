package testful.runner;

import java.io.EOFException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import testful.utils.CachingMap;
import testful.utils.Cloner;
import testful.utils.TestfulLogger;
import testful.utils.CachingMap.Cacheable;

public class WorkerManager implements IWorkerManager, ITestRepository {

	private static Logger logger = Logger.getLogger("testful.executor.worker");

	/** cache size: number of jobs to keep in local cache */
	private static final int CACHE_SIZE = 50;

	public static void createLocalWorkers(ITestRepository executor) {
		WorkerManager wm = new WorkerManager(-1, 0);
		wm.addTestRepository(executor);
	}

	private volatile boolean running = true;

	private final BlockingQueue<Context<?, ?>> tests;
	private final Map<String, ITestRepository> results;

	private final static int MAX_ELEMS = 20;
	private final static long MIN_AGE = 15 * 60 * 1000; // 15 min
	private final static long MIN_UNUSED = 5 * 60 * 1000; //  5 min

	private final CachingMap<String, ClassFinder> classFinders;
	private final CachingMap<String, Queue<TestfulClassLoader>> classLoaders;

	private AtomicLong executedJobs = new AtomicLong();
	
	private AtomicLong receivedBytes = new AtomicLong();
	private AtomicLong sentBytes = new AtomicLong();

	public WorkerManager(int cpu, int buffer) {
		logger.info("Starting: Worker Manager (" + TestfulLogger.singleton.runId + ")");

		if(buffer <= 0) buffer = CACHE_SIZE;
		tests = new ArrayBlockingQueue<Context<?, ?>>(buffer);
		results = new ConcurrentHashMap<String, ITestRepository>();

		classFinders = new CachingMap<String, ClassFinder>(MAX_ELEMS, MIN_AGE, MIN_UNUSED);
		classLoaders = new CachingMap<String, Queue<TestfulClassLoader>>(MAX_ELEMS, MIN_AGE, MIN_UNUSED);

		if(cpu < 0) cpu = Runtime.getRuntime().availableProcessors();
		for(int i = 0; i < cpu; i++)
			createWorker();

		String msg = "Started " + cpu + " workers";
		logger.info(msg);
		System.out.println(msg);
	}

	@Override
	public void addTestRepository(final ITestRepository rep) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String msg = "Added " + rep.getName();
					logger.info(msg);
					System.out.println(msg);

					while(running) {
						Context<?, ?> t = rep.getTest();
						receivedBytes.addAndGet(t.getSize());
						logger.fine("Retrieved test: " + t.id);
						results.put(t.id, rep);
						tests.put(t);
					}

				} catch(InterruptedException e) {
					String msg = "Interrupted: " + e.getMessage();
					logger.warning(msg);
					System.err.println(msg);
					return;
				} catch(RemoteException e) {
					if(e.getCause() instanceof EOFException) {
						System.out.println("Test Repository disconnected");
						logger.info("Test Repository disconnected");
						return;
					} else {
						String msg = "Cannot contact test repository: " + e.getMessage();
						logger.warning(msg);
						System.err.println(msg);
						return;
					}
				}
			}
		});

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
		return "runner-" + TestfulLogger.singleton.runId;
	}
	
	public void stop() {
		System.out.println("Stopping all jobs");

		try {
			running = false;
			TimeUnit.SECONDS.sleep(5);
		} catch(InterruptedException e) {
		}

		while(!tests.isEmpty()) {
			System.out.println("Waiting for " + tests.size() + " tests to execute...");
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch(InterruptedException e) {
			}
		}

		System.out.println("Bye\n");
		System.exit(0);
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
		ClassFinder classFinder = ctx.getClassFinder();
		String key = classFinder.getKey();

		Cacheable<ClassFinder> finder;
		synchronized(classFinders) {
			finder = classFinders.get(key);
			if(finder == null) {
				if(classFinder instanceof ClassFinderCaching) finder = new Cacheable<ClassFinder>(classFinder);
				else finder = new Cacheable<ClassFinder>(new ClassFinderCaching(classFinder));

				classFinders.put(key, finder);
			}
		}

		TestfulClassLoader ret = null;
		if(ctx.isRecycleClassLoader()) 
			synchronized(classLoaders) {
				Cacheable<Queue<TestfulClassLoader>> q = classLoaders.get(key);
				if(q != null) ret = q.getElement().poll();
			}

		if(ret == null) ret = new TestfulClassLoader(finder.getElement());

		return ret;
	}

	@Override
	public void putException(String key, byte[] exc) throws RemoteException {
		try {
			ITestRepository rep = results.remove(key);
			rep.putException(key, exc);
		} catch(Exception e) {
			String msg = "Cannot put the result back in the test repository: " + e.getMessage();
			logger.warning(msg);
			System.err.println(msg);
		}

		executedJobs.incrementAndGet();
		sentBytes.addAndGet(exc.length);
	}

	public void putException(Context<?, ?> ctx, Exception exc, TestfulClassLoader cl) {
		recycleClassLoader(cl);

		try {
			putException(ctx.id, Cloner.serialize(exc, true));
		} catch(RemoteException e) {
			// never happens
		}
	}

	@Override
	public void putResult(String key, byte[] result) throws RemoteException {
		try {
			ITestRepository rep = results.remove(key);
			rep.putResult(key, result);
		} catch(Exception e) {
			String msg = "Cannot put the result back in the test repository: " + e.getMessage();
			logger.warning(msg);
			System.err.println(msg);
		}

		executedJobs.incrementAndGet();
		sentBytes.addAndGet(result.length);
	}

	public void putResult(Context<?, ?> ctx, Serializable result, TestfulClassLoader cl) {
		recycleClassLoader(cl);

		try {
			putResult(ctx.id, Cloner.serialize(result, true));
		} catch(RemoteException e) {
			// never happens
		}
	}

	private void recycleClassLoader(TestfulClassLoader cl) {
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
		StringBuilder sb = new StringBuilder("Testful's runner - ").append(new Date().toString());

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
