/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010 Matteo Miraz
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

package testful.model.transformation;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * All automatic tests
 * @author matteo
 */
public class AllAutoTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllAutoTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(DynamicTransformationAutoTestCase.class);
		suite.addTestSuite(ReferenceSorterAutoTestCase.class);
		suite.addTestSuite(RemoveInvalidOperationsStaticAutoTestCase.class);
		suite.addTestSuite(TestReferenceSorterAutoTestCase.class);
		suite.addTestSuite(Splitter_splitObserverAutoTestCase.class);
		suite.addTest(AllTests.suite());
		suite.addTestSuite(Splitter_splitAutoTestCase.class);
		suite.addTestSuite(Splitter_splitAndMinimizeAutoTestCase.class);
		suite.addTestSuite(TestSplitter_splitMergeAutoTestCase.class);
		suite.addTestSuite(RemoveUselessDefsAutoTestCase.class);
		suite.addTestSuite(TestReorganizerAutoTestCase.class);
		suite.addTestSuite(RunnerCachingPrepareAutoTestCase.class);
		//$JUnit-END$
		return suite;
	}

	public static void main(String[] args) {
		TestRunner runner = new TestRunner();
		TestResult result = runner.doRun(suite(), false);

		if (!result.wasSuccessful())
			System.exit(1);
	}
}
