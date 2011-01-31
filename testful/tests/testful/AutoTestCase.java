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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import testful.coverage.CoverageExecutionManager;
import testful.coverage.CoverageInformation;
import testful.model.Operation;
import testful.model.Test;
import testful.runner.Context;
import testful.utils.ElementManager;
import ec.util.MersenneTwisterFast;

/**
 * @author matteo
 */
public abstract class AutoTestCase extends GenericTestCase {

	private static final int ITERATIONS = TestFul.getProperty(TestProperties.PROPERTY_TEST_RANDOM_ITERATIONS, 1000);

	/** Performs an extensive random testing session */
	private static final boolean EXTENSIVE_TEST = TestFul.getProperty(TestProperties.PROPERTY_TEST_RANDOM_EXTENSIVE, false);

	protected abstract Collection<? extends Test> perform(Test test) throws Exception;

	protected String[] getCuts() {
		return new String[] {
				// "apache.FractionNoStatic",
				"dummy.Simple",
				"dummy.StateMachine",
				"test.coverage.ControlFlow",
				"test.coverage.DataFlow",
				"test.coverage.DataFlowCtx",
				"test.coverage.Fault",
				"test.coverage.Stopped",
				"test.model.array.StringMatrix"
		};
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// ensure that static initializers are executed
		for(String cut : getCuts())
			getCoverage(createRandomTest(cut, 1000, 17l));

	}

	public void testSetup() throws Exception {
		for(String cut : getCuts()) {
			if(EXTENSIVE_TEST)
				System.out.print("   <setup>   ");
			else
				System.out.print("setup");

			assertEquals("Setup correctly your environment!", true, autoTest(cut, 500, 17l));
		}
	}

	public void testAllRnd() throws Exception {
		for(String cut : getCuts()) {
			MersenneTwisterFast r = new MersenneTwisterFast(37);
			for(int n = 0; n < ITERATIONS; n++) {
				final int size = 49+r.nextInt(950);
				final long seed = r.nextLong();

				if(EXTENSIVE_TEST) {
					for (int i = 1; i <= size; i++) {
						System.out.printf("%3.0f%% - %4.1f%% ", 100.0*n/ITERATIONS, 100.0*(i-1)/size);
						autoTest(cut, i, seed);
					}

				} else {
					System.out.printf("%3.0f%% ", 100.0*n/ITERATIONS);
					autoTest(cut, size, seed);
				}
			}
		}
	}

	protected boolean autoTest(String cut, int size, long seed) throws Exception {
		System.out.printf("[%40s] on %30s seed: %20d ", this.getClass().getSimpleName(), cut, seed);
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
				System.err.println("WARNING: phantom error!\n" + e.getMessage());
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
		Collection<? extends Test> res = perform(orig);
		long minStop = System.nanoTime();

		List<ElementManager<String, CoverageInformation>> partsCov = new ArrayList<ElementManager<String,CoverageInformation>>();
		ElementManager<String, CoverageInformation> combinedCov;
		if(res.size() == 1) partsCov.add(combinedCov = getCoverage(res.iterator().next()));
		else {
			combinedCov = new ElementManager<String, CoverageInformation>();

			List<Future<ElementManager<String, CoverageInformation>>> futures = new ArrayList<Future<ElementManager<String,CoverageInformation>>>();
			for(Test r : res) {
				Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(getFinder(), r, true);
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

		return String.format("%3d --(%5.1fms)--> %3d", orig.getTest().length, (minStop - minStart)/1000000.0, rLength);
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
			for(int i = initOps; i >= 0; i--) {

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
