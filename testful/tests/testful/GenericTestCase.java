package testful;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import junit.framework.TestCase;
import testful.coverage.CoverageExecutionManager;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestfulProblem.TestfulConfig;
import testful.runner.ClassFinder;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.Context;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;
import ec.util.MersenneTwisterFast;

/**
 * @author matteo
 *
 */
public abstract class GenericTestCase  extends TestCase {

	protected final static Configuration config = new Configuration();

	protected static final TestFailedException SETUP = new TestFailedException("Please setup correctly your system!");

	protected static boolean RECYCLE_CLASS_LOADER = true;
	protected boolean SKIP_CONTRACTS = false;

	public static class TestFailedException extends Exception {
		private static final long serialVersionUID = 1L;

		public final String[] differentCovs;

		public TestFailedException(String msg) {
			this(msg,null);
		}

		public TestFailedException(String msg, String differentCov) {
			super(msg);
			if(differentCov == null) this.differentCovs = null;
			else {
				String[] covs = differentCov.split(":");
				Arrays.sort(covs);
				this.differentCovs = covs;
			}
		}
	}

	protected void checkTestFailed(Test orig, ElementManager<String, CoverageInformation> origCov, List<Test> parts, List<ElementManager<String, CoverageInformation>> partsCov, ElementManager<String,CoverageInformation> combinedCov) throws TestFailedException {
		StringBuilder msg = new StringBuilder();

		boolean err = false;
		for(CoverageInformation o : origCov) {
			if(SKIP_CONTRACTS && !o.getKey().endsWith("n")) continue;

			float origQ = o.getQuality();

			CoverageInformation c = combinedCov.get(o.getKey());
			float combinedQ = (c == null ? 0 : c.getQuality());

			if(origQ != combinedQ) {
				err = true;
				break;
			}
		}

		if(!err) return;

		String differentCovs = null;
		for(CoverageInformation o : origCov) {
			float origQ = o.getQuality();

			CoverageInformation c = combinedCov.get(o.getKey());
			float combinedQ = (c == null ? 0 : c.getQuality());

			if(origQ != combinedQ) {
				if(differentCovs == null) differentCovs = o.getKey();
				else differentCovs += ":" + o.getKey();
				msg.append(o.getKey()).append(" ").append(origQ).append("-").append(combinedQ).append(" ");
			}
		}

		msg.append("\n");

		printTest(msg, "Original Test", orig, origCov);

		printTest(msg, "Modified Test", null, combinedCov);

		int i = 0;
		Iterator<Test> testIt = parts.iterator();
		Iterator<ElementManager<String, CoverageInformation>> covIt = partsCov.iterator();
		while(testIt.hasNext())
			printTest(msg, "Part " + ++i, testIt.next(), covIt.next());

		throw new TestFailedException(msg.toString(), differentCovs);
	}

	protected void printTest(StringBuilder msg, String name, Test test, ElementManager<String, CoverageInformation> covs) {
		msg.append(name).append(":\n");

		if(test != null) {
			msg.append(test);
			msg.append("---------");
		}

		msg.append("\nCoverage:\n");
		for(CoverageInformation cov : covs)
			msg.append("  ").append(cov.getKey()).append(" ").append(cov.getQuality()).append("\n");
		msg.append("---------\n");
	}

	private static IRunner exec;
	protected static IRunner getExec() {
		if(exec == null) exec = RunnerPool.createExecutor(null, false);
		return exec;
	}

	protected static ElementManager<String, CoverageInformation> getCoverage(Test test, TrackerDatum ... data) throws RemoteException, InterruptedException, ExecutionException {
		Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = 
			CoverageExecutionManager.getContext(getFinder(), test, data);
				
		ctx.setRecycleClassLoader(RECYCLE_CLASS_LOADER);
		Future<ElementManager<String, CoverageInformation>> future = getExec().execute(ctx);
		ElementManager<String, CoverageInformation> coverage = future.get();

		return coverage;
	}

	public Test createRandomTest(String cut, int lenght, long seed) throws RemoteException, ClassNotFoundException, TestfulException {
		MersenneTwisterFast random = new MersenneTwisterFast(seed);

		final TestfulConfig testfulConfig = new TestfulConfig();
		testfulConfig.setCut(cut);
		
		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), testfulConfig);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 4, 4);

		Operation[] ops = new Operation[lenght];
		for(int i = 0; i < ops.length; i++)
			ops[i] = Operation.randomlyGenerate(cluster, refFactory, random);

		Test t = new Test(cluster, refFactory, ops);
		return t;
	}

	private static ClassFinder finder = null;
	protected static ClassFinder getFinder() throws RemoteException {
		if(finder == null)
			finder = new ClassFinderCaching(new ClassFinderImpl(new File(config.getDirInstrumented()), new File(config.getDirJml()), new File(config.getDirVanilla())));

		return finder;
	}

}
