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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for testful.model");
		//$JUnit-BEGIN$
		//suite.addTestSuite(ExecutorTestCase.class);

		suite.addTestSuite(OptimalTestCase.class);

		suite.addTestSuite(Test_removeUselessDefsTestCase.class);
		suite.addTestSuite(Test_getSSATestCase.class);
		suite.addTestSuite(Test_sortRerefencesTestCase.class);
		suite.addTestSuite(Test_simplifyTestCase.class);
		suite.addTestSuite(Test_reorganizerTestCase.class);
		suite.addTestSuite(Test_reorganizerSplitTestCase.class);
		suite.addTestSuite(Test_reorganizerSortSplitTestCase.class);

		suite.addTestSuite(TestSplitter_splitTestCase.class);
		suite.addTestSuite(TestSplitter_splitObserverTestCase.class);
		suite.addTestSuite(TestSplitter_splitMergeTestCase.class);
		suite.addTestSuite(TestSplitter_splitAndMinimizeTestCase.class);
		//$JUnit-END$
		return suite;
	}

}
