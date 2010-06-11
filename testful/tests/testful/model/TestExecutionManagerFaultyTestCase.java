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

package testful.model;

import testful.FaultTestCase;

/**
 * Test for the test execution manager (which collects the OperationResult of tests).
 * Uses the test.coverage.Fault class.
 * @author matteo
 */
public class TestExecutionManagerFaultyTestCase extends FaultTestCase {

	public void testA() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[0], oCns, new Reference[] { }),
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a, new Reference[] { objects[0] } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(3, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[2].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[2].getInfo(OperationResult.KEY)).status);
	}

	public void testANull() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a, new Reference[] { objects[0] } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testA1() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[0], oCns, new Reference[] { }),
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a1, new Reference[] { objects[0] } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(3, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[2].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[2].getInfo(OperationResult.KEY)).status);
	}

	public void testA1Null() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a1, new Reference[] { objects[0] } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testA2() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[0], oCns, new Reference[] { }),
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a2, new Reference[] { objects[0] } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(3, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[2].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[2].getInfo(OperationResult.KEY)).status);
	}

	public void testA2Null() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a2, new Reference[] { objects[0] } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testB() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], b , new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testB1() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], b1, new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testC() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], c , new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testC1() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], c1, new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testC2() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], c2, new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testD() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], d , new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testE() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], e , new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testE1() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], e1, new Reference[] { } )
		});

		Operation[] testOperations = getOpResult(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}
}
