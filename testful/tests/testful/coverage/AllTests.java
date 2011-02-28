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

package testful.coverage;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs all tests within the testful.coverage package
 * @author matteo
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());

		//$JUnit-BEGIN$
		suite.addTestSuite(FaultTestCase.class);
		suite.addTestSuite(CoverageBehavioralTestCase.class);
		suite.addTestSuite(CoverageCFGTestCase.class);
		suite.addTestSuite(CoverageDataFlowTestCase.class);
		suite.addTestSuite(CoveragePUseTestCase.class);
		suite.addTestSuite(StopperTestCase.class);
		//$JUnit-END$

		return suite;
	}
}
