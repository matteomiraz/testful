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
