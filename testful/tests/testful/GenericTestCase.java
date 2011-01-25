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

package testful;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import junit.framework.TestCase;
import testful.coverage.CoverageExecutionManager;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.model.Operation;
import testful.model.OperationInformation;
import testful.model.OperationPosition;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.runner.ClassType;
import testful.runner.Context;
import testful.runner.DataFinder;
import testful.runner.DataFinderCaching;
import testful.runner.DataFinderImpl;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;
import ec.util.MersenneTwisterFast;

/**
 * @author matteo
 *
 */
public abstract class GenericTestCase extends TestCase {

	//-------------------------- logging -------------------------------------
	static {
		TestFul.setupLogging(GenericTestCase.getConfig());
	}
	//-------------------------- config --------------------------------------
	/** Returns the configuration for the TestCut */
	public static IConfigProject getConfig() {
		ConfigProject tmp = new ConfigProject();
		tmp.setDirBase(new File("testCut"));
		tmp.setQuiet(true);
		return tmp;
	}

	//-------------------------- class finder --------------------------------
	private static final DataFinderCaching finder;
	static {
		DataFinderCaching tmp;
		try {
			tmp = new DataFinderCaching(new DataFinderImpl(new ClassType(getConfig())));
		} catch (RemoteException e) {
			tmp = null;
			fail(e.getMessage());
		}
		finder = tmp;
	}
	/** Returns the class finder for the TestCut */
	public static DataFinder getFinder() {
		return finder;
	}

	//-------------------------- Runner --------------------------------------
	static {
		TestFul.setupLogging(GenericTestCase.getConfig());
		RunnerPool.getRunnerPool().startLocalWorkers();
	}
	private static IRunner exec;
	/** Returns the executor  */
	protected static IRunner getExec() {
		if(exec == null)
			exec = RunnerPool.getRunnerPool();

		return exec;
	}

	//----------------------- TestFailedException ----------------------------

	/**
	 * Exception to signal that a test failed.
	 * @author matteo
	 */
	public static class TestFailedException extends Exception {
		private static final long serialVersionUID = 1L;

		public final String[] differentCovs;

		public TestFailedException(String msg) {
			this(msg,null);
		}

		public TestFailedException(String msg, String differentCov) {
			super(msg);
			if(differentCov == null) differentCovs = null;
			else {
				String[] covs = differentCov.split(":");
				Arrays.sort(covs);
				differentCovs = covs;
			}
		}
	}

	protected static final TestFailedException SETUP = new TestFailedException("Please setup correctly your system!");

	//------------------------- Utility methods ------------------------------

	public static <T> void createArrayAssertions(T[] a, String var) {
		System.err.println("\n// " + var);

		if(a == null) {
			System.err.println("assertNull("+ var +");");
			return;
		}

		System.err.println("assertEquals(" + a.length + ", " + var + ".length);");
		for (int i = 0; i < a.length; i++)
			System.err.println("assertEquals(\"" + a[i].toString() + "\", " + var  + "[" + i + "].toString());");
	}

	public static void createCoverageAssertions(CoverageInformation cov) {
		System.err.println("assertEquals(" + cov.getQuality() + "f, cov.getQuality());");
		System.err.println("assertEquals(\"" + cov.toString().replaceAll("\n", "\\\\n") + "\", cov.toString());");
	}

	public static void createCoveragesAssertions(ElementManager<String, CoverageInformation> covs) {
		for (CoverageInformation cov : covs) {
			System.err.println("{");
			System.err.println("  CoverageInformation cov = covs.get(" + cov.getClass().getCanonicalName() + ".KEY);");
			System.err.println("  assertNotNull(cov);");
			System.err.println("  assertEquals(" + cov.getQuality() + "f, cov.getQuality());");
			System.err.println("  assertEquals(\"" + cov.toString().replaceAll("\n", "\\\\n") + "\", cov.toString());");
			System.err.println("}");
		}
	}

	public static Test createRandomTest(String cut, int lenght, long seed) throws RemoteException, ClassNotFoundException, TestfulException {
		MersenneTwisterFast random = new MersenneTwisterFast(seed);

		ConfigCut testfulConfig = new ConfigCut(getConfig());
		testfulConfig.setCut(cut);

		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), testfulConfig);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 4, 4);

		Operation[] ops = new Operation[lenght];
		for(int i = 0; i < ops.length; i++)
			ops[i] = Operation.randomlyGenerate(cluster, refFactory, random);

		return new Test(cluster, refFactory, ops);
	}

	protected static ElementManager<String, CoverageInformation> getCoverage(Test test, TrackerDatum ... data) throws InterruptedException, ExecutionException {
		Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx =
			CoverageExecutionManager.getContext(getFinder(), test, data);

		ctx.setRecycleClassLoader(true);
		Future<ElementManager<String, CoverageInformation>> future = getExec().execute(ctx);
		ElementManager<String, CoverageInformation> coverage = future.get();

		return coverage;
	}

	private static final Comparator<Operation[]> dummyTestComparator = new Comparator<Operation[]>() {
		@Override
		public int compare(Operation[] o1, Operation[] o2) {
			if(o1.length != o2.length) return o1.length - o2.length;

			return Arrays.hashCode(o1) - Arrays.hashCode(o2);
		}
	};

	/**
	 * Verifies the outcome of a transformation.
	 * @param original The original test
	 * @param tests the transformed tests
	 * @param expected the expected outcome
	 * @param checkCoverage if true, also verifies that the expected outcome have the same coverage of the original test
	 */
	protected void check(Test original, Collection<? extends Test> tests, Operation[][] expected, boolean checkCoverage) throws Exception {

		// checks if the original test and the expected one have the same level of coverage
		if(checkCoverage) {
			ElementManager<String, CoverageInformation> oCovs = getCoverage(original);
			ElementManager<String, CoverageInformation> tCovs = new ElementManager<String, CoverageInformation>();
			for (Operation[] exp : expected) {
				for (CoverageInformation cov : getCoverage(new Test(original.getCluster(), original.getReferenceFactory(), exp))) {
					CoverageInformation tCov = tCovs.get(cov.getKey());
					if(tCov == null) tCovs.put(cov);
					else tCov.merge(cov);
				}
			}

			for (CoverageInformation oCov : oCovs) {
				if(oCov.getQuality() == 0) continue;

				CoverageInformation tCov = tCovs.get(oCov.getKey());

				if(tCov == null) print(original, tests);
				assertNotNull("No " + oCov.getName() + " information", tCov);

				if(oCov.getQuality() != tCov.getQuality()) {
					print(original, tests);
					// System.err.println("Original " + oCov.getName() + ": " + oCov);
					// System.err.println("Oracle's " + oCov.getName() + ": " + tCov);
				}
				assertEquals("Wrong " + oCov.getName(), oCov.getQuality(), tCov.getQuality());
			}
		}

		if(expected.length != tests.size()) print(original, tests);
		assertEquals("Wrong number of results", expected.length, tests.size());

		Operation[][] actual = new Operation[tests.size()][];
		{
			int i = 0;
			for(Test t1 : tests) actual[i++] = t1.getTest();
		}

		Arrays.sort(expected, dummyTestComparator);
		Arrays.sort(actual, dummyTestComparator);

		for(int i = 0; i < expected.length; i++) {

			if(expected[i].length != actual[i].length) print(original, tests);
			assertEquals("Test " + i + ": wrong result size", expected[i].length, actual[i].length);

			for(int j = 0; j < expected[i].length; j++) {
				if(!expected[i][j].toString().equals(actual[i][j].toString())) {
					print(original, tests);
					assertEquals("Test " + i + ": Mismatch in operation " + j, expected[i][j].toString(), actual[i][j].toString());
				}
			}
		}
	}

	private void print(Test original, Collection<? extends Test> tests) {
		System.err.println("original:");

		int n = -1;
		for(Operation o : original.getTest()) {
			n++;
			final OperationInformation info = o.getInfo(OperationPosition.KEY);
			System.err.println((info!=null?info:"Operation #" + Integer.toString(n)) + "\t" + o);
		}
		System.err.println("---");

		System.err.println("Modified: " + tests.size());
		for(Test t1 : tests) {
			for(Operation o : t1.getTest()) {
				final OperationInformation info = o.getInfo(OperationPosition.KEY);
				System.err.println((info!=null?info:"") + "\t" + o);
			}
			System.err.println("---");
		}
	}

	protected void checkTestFailed(Test orig, ElementManager<String, CoverageInformation> origCov, Collection<? extends Test> parts, List<ElementManager<String, CoverageInformation>> partsCov, ElementManager<String,CoverageInformation> combinedCov) throws TestFailedException {
		StringBuilder msg = new StringBuilder();

		boolean err = false;
		for(CoverageInformation o : origCov) {
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
		Iterator<? extends Test> testIt = parts.iterator();
		Iterator<ElementManager<String, CoverageInformation>> covIt = partsCov.iterator();
		while(testIt.hasNext())
			printTest(msg, "Part " + ++i, testIt.next(), covIt.next());

		throw new TestFailedException(msg.toString(), differentCovs);
	}

	private void printTest(StringBuilder msg, String name, Test test, ElementManager<String, CoverageInformation> covs) {
		msg.append(name).append(":\n");

		if(test != null) {
			msg.append(test);
			msg.append("---------");
		}

		if(covs != null) {
			msg.append("\nCoverage:\n");
			for(CoverageInformation cov : covs)
				msg.append("  ").append(cov.getKey()).append(" ").append(cov.getQuality()).append("\n");
			msg.append("---------");
		}

		msg.append("\n");
	}
}
