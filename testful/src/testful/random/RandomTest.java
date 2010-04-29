package testful.random;

import java.io.File;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.coverage.CoverageInformation;
import testful.coverage.RunnerCaching;
import testful.coverage.TrackerDatum;
import testful.model.Operation;
import testful.model.OptimalTestCreator;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestCoverage;
import testful.model.TestSuite;
import testful.runner.ClassFinder;
import testful.utils.ElementManager;
import ec.util.MersenneTwisterFast;

public abstract class RandomTest {
	protected static Logger logger = Logger.getLogger("testful.random");

	protected long start, stop;
	private long numCall;

	protected final RunnerCaching runner;

	protected final TestCluster cluster;
	protected final ReferenceFactory refFactory;

	private AtomicInteger testsDone = new AtomicInteger();

	protected final BlockingQueue<Entry<Operation[], Future<ElementManager<String, CoverageInformation>>>> tests = new LinkedBlockingQueue<Entry<Operation[], Future<ElementManager<String, CoverageInformation>>>>();
	private final OptimalTestCreator optimal;
	protected final ClassFinder finder;
	protected final TrackerDatum[] data;

	protected final MersenneTwisterFast random;

	protected volatile boolean keepRunning = true;

	protected final File logDirectory;

	public RandomTest(boolean enableCache, File logDirectory, ClassFinder finder, TestCluster cluster, ReferenceFactory refFactory, TrackerDatum ... data) {
		this.logDirectory = logDirectory;
		optimal = new OptimalTestCreator();

		long seed = System.currentTimeMillis();
		logger.config("MersenneTwisterFast: seed=" + seed);
		random = new MersenneTwisterFast(seed);

		runner = new RunnerCaching(enableCache);

		this.cluster = cluster;
		this.refFactory = refFactory;

		this.finder = finder;
		this.data = data;
	}

	protected abstract void work(long duration);

	public final void test(long duration) {
		startNotificationThread();

		work(duration);

		try {
			while(getRunningJobs() > 0)
				Thread.sleep(1000);
		} catch(InterruptedException e) {}

		keepRunning = false;
	}

	public ElementManager<String, CoverageInformation> getExecutionInformation() {
		return optimal.getCoverage();
	}

	public int getRunningJobs() {
		return tests.size();
	}

	private void startNotificationThread() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while(keepRunning)
					try {
						Entry<Operation[], Future<ElementManager<String, CoverageInformation>>> entry = tests.take();
						ElementManager<String, CoverageInformation> cov = entry.getValue().get();
						testsDone.incrementAndGet();
						numCall += entry.getKey().length;

						final TestCoverage testCoverage = new TestCoverage(new Test(cluster, refFactory, entry.getKey()), cov);
						optimal.update(testCoverage);
					} catch(InterruptedException e) {
						logger.log(Level.WARNING, "Interrupted: " + e.getMessage(), e);
					} catch(ExecutionException e) {
						logger.log(Level.WARNING, "Error during a test evaluation: " + e.getMessage(), e);
					}
			}
		}, "futureWaiter");
		t.setDaemon(true);
		t.start();

		t = new Thread(new Runnable() {

			@Override
			public void run() {
				while(keepRunning) {
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch(InterruptedException e) {
						return;
					}

					long now = System.currentTimeMillis();

					optimal.log(null, numCall, (now - start));

					runner.updateCacheScore();

					if(logger.isLoggable(Level.INFO)) {
						StringBuilder sb = new StringBuilder();

						long remaining = (stop - now) / 1000;

						sb.append(String.format("%5.2f%% %d:%02d to go ",
								(100.0 * (now - start))/(stop-start),
								remaining / 60,
								remaining % 60
						));


						sb.append("Running ").append(getRunningJobs()).append(" jobs (").append(testsDone.get()).append(" done)\n");
						if(runner.isEnabled()) sb.append(runner).append("\n");

						if(!optimal.getCoverage().isEmpty()) {
							sb.append("Coverage:\n");
							for(CoverageInformation info : optimal.getCoverage())
								sb.append("  ").append(info.getName()).append(": ").append(info.getQuality()).append("\n");
						}

						logger.info(sb.toString());
					}
				}
			}
		}, "notification");

		t.setDaemon(true);
		t.start();
	}

	/**
	 * @author Tudor
	 * @return The list of Tests(OpSequences) generated and selected by RT
	 * Note: This function is useful only after it processes something :)
	 */
	public TestSuite getResults() {
		TestSuite ret = new TestSuite();
		for (TestCoverage test : optimal.get())
			ret.add(test);

		return ret;
	}
}
