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
import junit.framework.TestSuite;

/**
 * Tests (excluding random *AutoTestCase tests) for the transformation module
 * @author matteo
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(ReorganizerTestCase.class);
		suite.addTestSuite(SimplifierDynamicTestCase.class);
		suite.addTestSuite(SimplifierStaticTestCase.class);
		suite.addTestSuite(Splitter_splitObserverTestCase.class);
		suite.addTestSuite(Splitter_splitTestCase.class);
		suite.addTestSuite(Splitter_splitAndMinimizeTestCase.class);
		suite.addTestSuite(Splitter_splitAndMergeTestCase.class);
		//$JUnit-END$
		return suite;
	}

}
