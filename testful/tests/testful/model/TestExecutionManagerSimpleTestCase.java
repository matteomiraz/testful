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

import testful.testCut.DummySimpleTestCase;

/**
 * Test for the test execution manager (which collects the OperationResult of tests).
 * Uses the dummy.Simple class.
 * @author matteo
 */
public class TestExecutionManagerSimpleTestCase extends DummySimpleTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSimple1() throws Exception {

		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], mInc, new Reference[] { })
		});

		OperationResult.insert(test.getTest());
		Operation[] testOperations = TestExecutionManager.execute(getFinder(), test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);
	}

	public void testSimple2() throws Exception {

		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], mInc, new Reference[] { })
		});

		OperationResult.insert(test.getTest());
		Operation[] ops = TestExecutionManager.execute(getFinder(), test);
		for (int i = 0; i < ops.length; i++)
			ops[i] = ops[i].adapt(cluster, refFactory);
		Test test2 = new Test(cluster, refFactory, ops);

		OperationResult.insert(test2.getTest());
		Operation[] testOperations = TestExecutionManager.execute(getFinder(), test2);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);
	}
}
