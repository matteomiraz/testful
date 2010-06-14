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

import testful.testCut.TestCoverageStoppedTestCase;

/**
 * Test for the test execution manager (which collects the OperationResult of tests).
 * Uses the test.coverage.Stopped class.
 * @author matteo
 */
public class TestExecutionManagerStopperTestCase extends TestCoverageStoppedTestCase {

	public void testLongMethod1() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod1, new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(t);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testLongMethod2() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod2, new Reference[] { } )
		});


		Operation[] testOperations = getOpResult(t);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testLongMethod3() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod3, new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(t);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testLongMethod4() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod4, new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(t);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testLongMethod5() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod5, new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(t);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)t.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}
}
