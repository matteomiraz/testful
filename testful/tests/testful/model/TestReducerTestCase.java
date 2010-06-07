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


package testful.model;

import java.util.Collection;

import testful.coverage.TrackerDatum;
import testful.regression.TestSuiteReducer;

/**
 * @author matteo
 */
public class TestReducerTestCase extends AutoTestCase {

	@Override
	protected Collection<TestCoverage> perform(Test test) throws Exception {
		TestSuiteReducer reducer = new TestSuiteReducer(getFinder(), new TrackerDatum[0], true);

		reducer.process(test);

		return reducer.getOutput();
	}

	public void testFoo() throws Exception {
		getExec();

		Test t = createRandomTest("dummy.Simple", 500, 2568267209504662348l);

		Collection<TestCoverage> result = perform(t);


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
