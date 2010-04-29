package testful.model;

import java.util.List;

import testful.coverage.TrackerDatum;
import testful.regression.TestSuiteReducer;

/**
 * @author matteo
 */
public class TestReducerTestCase extends AutoTestCase {

	@Override
	protected List<TestCoverage> perform(Test test) throws Exception {
		TestSuiteReducer reducer = new TestSuiteReducer(getFinder(), new TrackerDatum[0]);

		reducer.process(test);

		return reducer.getOutput();
	}

	public void testFoo() throws Exception {
		getExec();

		Test t = createRandomTest("dummy.Simple", 500, 2568267209504662348l);

		List<TestCoverage> result = perform(t);


		for (TestCoverage p : result)
			System.err.println("PART:\n" + p + "\n----\n");

		/* Expecting the following three tests:
		 * 
		 * 1.java_lang_Integer_0 = (int)2147483647;
		 *   dummy_Simple_0 = new dummy.Simple();
		 *   dummy_Simple_0.oAbs();
		 *   dummy_Simple_0.mInc();
		 *   dummy_Simple_0.wModulo(java_lang_Integer_0);
		 * 
		 * 2.dummy_Simple_0 = new dummy.Simple();
		 *   dummy_Simple_0.oStatus();
		 *   dummy_Simple_0.mInc();
		 * 
		 * 3.dummy_Simple_0 = new dummy.Simple();
		 *   dummy_Simple_0.mDec();
		 *   dummy_Simple_0.oAbs();
		 */

		assertEquals(3, result.size());
	}
}
