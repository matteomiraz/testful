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

import testful.GenericTestCase;
import testful.testCut.DummySimpleCUT;
import testful.testCut.TestCoverageFaultCUT;
import testful.testCut.TestCoverageStoppedCUT;

/**
 * Test for the test execution manager (which collects the OperationResult of tests).
 * @author matteo
 */
public class TestExecutionManagerTestCase extends GenericTestCase {

	public void testFaultA() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.oCns, new Reference[] { }),
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a, new Reference[] { cut.objects[0] } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(3, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[2].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultANull() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a, new Reference[] { cut.objects[0] } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultA1() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.oCns, new Reference[] { }),
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a1, new Reference[] { cut.objects[0] } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(3, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[2].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultA1Null() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a1, new Reference[] { cut.objects[0] } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultA2() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.oCns, new Reference[] { }),
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a2, new Reference[] { cut.objects[0] } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(3, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[2].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultA2Null() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a2, new Reference[] { cut.objects[0] } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultB() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.b , new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultB1() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.b1, new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultC() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.c , new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultC1() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.c1, new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultC2() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.c2, new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultD() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.d , new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultE() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.e , new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testFaultE1() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.e1, new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.EXCEPTIONAL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testDummy1() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.mInc, new Reference[] { })
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testDummy2() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.mInc, new Reference[] { })
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testStoppedLongMethod1() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod1, new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testStoppedLongMethod2() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod2, new Reference[] { } )
		});


		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testStoppedLongMethod3() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod3, new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testStoppedLongMethod4() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod4, new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}

	public void testStoppedLongMethod5() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod5, new Reference[] { } )
		});

		OperationResult.insert(test.getTest());
		OperationResultTestExecutor.execute(getFinder(), test, true);

		Operation[] testOperations = test.getTest();
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.POSTCONDITION_ERROR, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);
	}
}
