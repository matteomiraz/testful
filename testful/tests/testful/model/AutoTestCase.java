package testful.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

import testful.GenericTestCase;
import testful.coverage.CoverageExecutionManager;
import testful.coverage.CoverageInformation;
import testful.model.TestSplitter.OperationPosition;
import testful.runner.Context;
import testful.utils.ElementManager;
import ec.util.MersenneTwisterFast;

/**
 * @author matteo
 *
 */
public abstract class AutoTestCase extends GenericTestCase {

	protected abstract List<? extends Test> perform(Test test) throws Exception;

	protected String[] getCuts() {
		return new String[] {
				"dummy.Simple",
		};
	}

	public void testSetup() throws Exception {
		for(String cut : getCuts())
			assertEquals("Setup correctly your environment!", true, autoTest(cut, 500, 17l));
	}

	public void testAllRnd() throws Exception {
		for(String cut : getCuts()) {
			MersenneTwisterFast r = new MersenneTwisterFast(37);
			for(int n = 1; n < 1000; n++) {
				System.out.printf("%5.1f%% ", n/10.0);
				autoTest(cut, 1000+r.nextInt(1000), r.nextLong());
			}
		}
	}

	protected void check(Test t, Operation[][] expected) throws Exception {
		List<? extends Test> tests = perform(t);

		System.out.println("original:");
		for(Operation o : t.getTest()) {
			final OperationInformation info = o.getInfo(OperationPosition.KEY);
			System.out.println((info!=null?info:"") + "\t" + o);
		}
		System.out.println("---");

		System.out.println("Modified: " + tests.size());
		for(Test t1 : tests) {
			for(Operation o : t1.getTest()) {
				final OperationInformation info = o.getInfo(OperationPosition.KEY);
				System.out.println((info!=null?info:"") + "\t" + o);
			}
			System.out.println("---");
		}

		assertEquals("Wrong number of results", expected.length, tests.size());

		Operation[][] actual = new Operation[tests.size()][];
		for(int i = 0; i < actual.length; i++)
			actual[i] = tests.get(i).getTest();

		Arrays.sort(expected, dummyTestComparator);
		Arrays.sort(actual, dummyTestComparator);

		for(int i = 0; i < expected.length; i++) {
			assertEquals("Test " + i + ": wrong result size", expected[i].length, actual[i].length);
			for(int j = 0; j < expected[i].length; j++)
				assertEquals("Test " + i + ": Mismatch in operation " + j, expected[i][j], actual[i][j]);
		}
	}
	private static final Comparator<Operation[]> dummyTestComparator = new Comparator<Operation[]>() {
		@Override
		public int compare(Operation[] o1, Operation[] o2) {
			if(o1.length != o2.length) return o1.length - o2.length;

			return Arrays.hashCode(o1) - Arrays.hashCode(o2);
		}
	};

	protected boolean autoTest(String cut, int size, long seed) throws Exception {
		System.out.print("Testing " + cut + " with " + size + " operations with seed: " + seed);
		System.out.flush();

		Test orig = createRandomTest(cut, size, seed);

		return autoTest(orig);
	}

	protected boolean autoTest(Test test) throws Exception, TestFailedException {
		try {
			String msg = executeTest(test);
			System.out.println(msg);
		} catch(TestFailedException e) {
			if(e == SETUP) {
				System.out.println(" -- empty coverage --");
				return false;
			}

			System.out.println(" BUG! " + Arrays.toString(e.differentCovs));
			Test reduced = reducer(test);

			try {
				executeTest(reduced);
				String msg = "WARNING: phantom error!\n" + e.getMessage();
				System.err.println(msg);
				fail(msg);
			} catch (TestFailedException e1) {
				System.err.println(e1.getMessage());
				fail(e1.getMessage());
			}
		}

		return true;
	}

	private String executeTest(Test orig) throws TestFailedException, Exception {

		ElementManager<String, CoverageInformation> origCov = getCoverage(orig);
		checkSetup(origCov);

		long minStart = System.nanoTime();
		List<? extends Test> res = perform(orig);
		long minStop = System.nanoTime();

		List<ElementManager<String, CoverageInformation>> partsCov = new ArrayList<ElementManager<String,CoverageInformation>>();
		ElementManager<String, CoverageInformation> combinedCov;
		if(res.size() == 1) partsCov.add(combinedCov = getCoverage(res.get(0)));
		else {
			combinedCov = new ElementManager<String, CoverageInformation>();

			List<Future<ElementManager<String, CoverageInformation>>> futures = new ArrayList<Future<ElementManager<String,CoverageInformation>>>();
			for(Test r : res) {
				Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(getFinder(), r);
				futures.add(getExec().execute(ctx));
			}
			for(Future<ElementManager<String, CoverageInformation>> future : futures) {
				ElementManager<String, CoverageInformation> rCov = future.get();
				partsCov.add(rCov);
				for(CoverageInformation cov : rCov) {
					CoverageInformation pre = combinedCov.get(cov.getKey());
					if(pre == null) {
						pre = cov.createEmpty();
						combinedCov.put(pre);
					}
					pre.merge(cov);
				}
			}
		}

		checkTestFailed(orig, origCov, res, partsCov, combinedCov);

		int rLength = 0;
		for(Test t : res) rLength += t.getTest().length;

		return " Original: " + orig.getTest().length +
		" Modified: " + rLength + " (" + String.format("%3.2f", (minStop - minStart)/1000000.0) + " ms)";
	}

	private void checkSetup(ElementManager<String, CoverageInformation> origCov) throws TestFailedException {
		if(origCov.isEmpty()) throw SETUP;

		boolean ok = false;
		for(CoverageInformation cov : origCov)
			if(cov.getQuality() > 0) ok = true;

		if(!ok) throw SETUP;
	}

	private Test reducer(Test test) throws Exception {

		String[] differentCovs = null;
		try {
			executeTest(test);
			return test;
		} catch(TestFailedException e) {
			differentCovs = e.differentCovs;
		}

		System.out.println("Reducer");

		List<Operation> ops = new ArrayList<Operation>(test.getTest().length);
		for(Operation operation : test.getTest()) ops.add(operation);

		Test reduced = null;
		int iter = 0;
		boolean changed = true;
		while(changed) {
			changed = false;
			System.out.println("  Iteration " + iter++ + " (" + ops.size() + ") ");
			int initOps = ops.size()-1;
			for(int i = initOps; i > 0; i--) {

				if((initOps-i) % 70 == 0) {
					if((initOps-i) != 0)
						System.out.printf("%6.2f%%\n   ", (100.0f * (initOps-i)) / initOps );
					else
						System.out.print("   ");
				}

				Operation op = ops.remove(i);
				reduced = new Test(test.getCluster(), test.getReferenceFactory(), ops.toArray(new Operation[ops.size()]));
				try {
					executeTest(reduced);
					ops.add(i, op);
					System.out.print(".");
				} catch(TestFailedException e) {
					if(e == SETUP) {
						ops.add(i, op);
						System.out.print("_");
					} else if(!Arrays.equals(differentCovs, e.differentCovs)) {
						ops.add(i, op);
						System.out.print(":");
					} else {
						System.out.print("O");
						changed = true;
					}
				}
			}

			System.out.println(" done (" + ops.size() + ")");
		}

		System.out.println("Reduced from " + test.getTest().length + " to " + ops.size());

		return new Test(test.getCluster(), test.getReferenceFactory(), ops.toArray(new Operation[ops.size()]));
	}
}
