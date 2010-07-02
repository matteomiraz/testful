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

package testful.coverage;

import testful.GenericTestCase;
import testful.coverage.fault.Fault;
import testful.coverage.fault.FaultsCoverage;
import testful.coverage.fault.UnexpectedExceptionException;
import testful.coverage.stopper.TestStoppedException;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.model.AssignPrimitive;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.testCut.TestCoverageStoppedCUT;
import testful.utils.ElementManager;

/**
 * Test for the stopper functionality
 * @author matteo
 */
public class StopperTestCase extends GenericTestCase {

	public void testLongMethod1() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod1, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(5.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod1(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testLongMethod1b() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod1, new Reference[] { } ),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(8.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod1(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testLongMethod2() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod2, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(5.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod2(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testLongMethod2b() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod2, new Reference[] { } ),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(8.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod2(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testLongMethod3() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod3, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(5.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod3(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testLongMethod3b() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod3, new Reference[] { } ),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(8.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod3(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testLongMethod4() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod4, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(5.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod4(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testLongMethod4b() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod4, new Reference[] { } ),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(8.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod4(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testLongMethod5() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod5, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(7.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod5(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testLongMethod5b() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.longMethod5, new Reference[] { } ),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(10.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Stopped.longMethod5(Stopped.java)", fault.getStackTrace()[0].toString());
	}

	public void testInfLoop1a() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1), // 1 recursion
				new AssignPrimitive(cut.ints[1], 0), // no delay => stackOverFlow
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.infLoop, new Reference[] { cut.ints[0], cut.ints[1] } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);

		assertEquals(6.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), UnexpectedExceptionException.class.getCanonicalName());
		assertEquals(fault.getCauseExceptionName(), "java.lang.StackOverflowError");

		final StackTraceElement[] stackTrace = fault.getStackTrace();
		assertEquals(3, fault.getStackTrace().length);
		assertEquals(" --  recursion.end  -- ()", stackTrace[0].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", stackTrace[1].toString());
		assertEquals(" -- recursion.start -- ()", stackTrace[2].toString());
	}

	public void testInfLoop1b() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),  // 1 recursion
				new AssignPrimitive(cut.ints[1], 10), // 10 ms of delay => Test Stopped
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.infLoop, new Reference[] { cut.ints[0], cut.ints[1] } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);

		assertEquals(10.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		final StackTraceElement[] stackTrace = fault.getStackTrace();
		assertEquals(3, fault.getStackTrace().length);
		assertEquals(" --  recursion.end  -- ()", stackTrace[0].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", stackTrace[1].toString());
		assertEquals(" -- recursion.start -- ()", stackTrace[2].toString());
	}

	public void testInfLoop2a() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 2), // 2 recursion
				new AssignPrimitive(cut.ints[1], 0), // no delay => stackOverFlow
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.infLoop, new Reference[] { cut.ints[0], cut.ints[1] } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);

		assertEquals(9.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), UnexpectedExceptionException.class.getCanonicalName());
		assertEquals(fault.getCauseExceptionName(), "java.lang.StackOverflowError");

		final StackTraceElement[] stackTrace = fault.getStackTrace();
		assertEquals(4, fault.getStackTrace().length);
		assertEquals(" --  recursion.end  -- ()", stackTrace[0].toString());

		// it the StackOverflow is throw, the initial element is not known
		assertTrue(
				( // correct order
						"test.coverage.Stopped.infLoop2(Stopped.java)".equals(stackTrace[1].toString()) &&
						"test.coverage.Stopped.infLoop(Stopped.java)".equals(stackTrace[2].toString())
				) || (
						"test.coverage.Stopped.infLoop(Stopped.java)".equals(stackTrace[1].toString()) &&
						"test.coverage.Stopped.infLoop2(Stopped.java)".equals(stackTrace[2].toString())
				)
		);

		assertEquals(" -- recursion.start -- ()", stackTrace[3].toString());
	}

	public void testInfLoop2b() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 2),  // 2 recursion
				new AssignPrimitive(cut.ints[1], 10), // 10 ms of delay => Test Stopped
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.infLoop, new Reference[] { cut.ints[0], cut.ints[1] } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);

		assertEquals(15.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		final StackTraceElement[] stackTrace = fault.getStackTrace();
		assertEquals(4, fault.getStackTrace().length);
		assertEquals(" --  recursion.end  -- ()", stackTrace[0].toString());
		assertEquals("test.coverage.Stopped.infLoop2(Stopped.java)", stackTrace[1].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", stackTrace[2].toString());
		assertEquals(" -- recursion.start -- ()", stackTrace[3].toString());
	}

	public void testInfLoop3a() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 3), // 3 recursion
				new AssignPrimitive(cut.ints[1], 0), // no delay => stackOverFlow
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.infLoop, new Reference[] { cut.ints[0], cut.ints[1] } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);

		assertEquals(12.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), UnexpectedExceptionException.class.getCanonicalName());
		assertEquals(fault.getCauseExceptionName(), "java.lang.StackOverflowError");

		final StackTraceElement[] stackTrace = fault.getStackTrace();
		assertEquals(5, fault.getStackTrace().length);
		assertEquals(" --  recursion.end  -- ()", stackTrace[0].toString());

		// it the StackOverflow is throw, the initial element is not known
		assertTrue(
				( // correct order
						"test.coverage.Stopped.infLoop2(Stopped.java)".equals(stackTrace[1].toString()) &&
						"test.coverage.Stopped.infLoop3(Stopped.java)".equals(stackTrace[2].toString()) &&
						"test.coverage.Stopped.infLoop(Stopped.java)".equals(stackTrace[3].toString())
				) || (
						"test.coverage.Stopped.infLoop2(Stopped.java)".equals(stackTrace[2].toString()) &&
						"test.coverage.Stopped.infLoop3(Stopped.java)".equals(stackTrace[3].toString()) &&
						"test.coverage.Stopped.infLoop(Stopped.java)".equals(stackTrace[1].toString())
				) || (
						"test.coverage.Stopped.infLoop2(Stopped.java)".equals(stackTrace[3].toString()) &&
						"test.coverage.Stopped.infLoop3(Stopped.java)".equals(stackTrace[1].toString()) &&
						"test.coverage.Stopped.infLoop(Stopped.java)".equals(stackTrace[2].toString())
				)
		);

		assertEquals(" -- recursion.start -- ()", stackTrace[4].toString());
	}

	public void testInfLoop3b() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 3),  // 3 recursion
				new AssignPrimitive(cut.ints[1], 10), // 10 ms of delay => Test Stopped
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.infLoop, new Reference[] { cut.ints[0], cut.ints[1] } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);

		assertEquals(20.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		final StackTraceElement[] stackTrace = fault.getStackTrace();
		assertEquals(5, fault.getStackTrace().length);
		assertEquals(" --  recursion.end  -- ()", stackTrace[0].toString());
		assertEquals("test.coverage.Stopped.infLoop2(Stopped.java)", stackTrace[1].toString());
		assertEquals("test.coverage.Stopped.infLoop3(Stopped.java)", stackTrace[2].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)",  stackTrace[3].toString());
		assertEquals(" -- recursion.start -- ()", stackTrace[4].toString());
	}

	public void testInfLoop4a() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 4), // 4 recursion
				new AssignPrimitive(cut.ints[1], 0), // no delay => stackOverFlow
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.infLoop, new Reference[] { cut.ints[0], cut.ints[1] } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);

		assertEquals(15.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), UnexpectedExceptionException.class.getCanonicalName());
		assertEquals(fault.getCauseExceptionName(), "java.lang.StackOverflowError");

		final StackTraceElement[] stackTrace = fault.getStackTrace();
		assertEquals(6, fault.getStackTrace().length);
		assertEquals(" --  recursion.end  -- ()", stackTrace[0].toString());

		// it the StackOverflow is throw, the initial element is not known
		assertTrue(
				( // correct order
						"test.coverage.Stopped.infLoop2(Stopped.java)".equals(stackTrace[1].toString()) &&
						"test.coverage.Stopped.infLoop3(Stopped.java)".equals(stackTrace[2].toString()) &&
						"test.coverage.Stopped.infLoop4(Stopped.java)".equals(stackTrace[3].toString()) &&
						"test.coverage.Stopped.infLoop(Stopped.java)" .equals(stackTrace[4].toString())
				) || (
						"test.coverage.Stopped.infLoop2(Stopped.java)".equals(stackTrace[2].toString()) &&
						"test.coverage.Stopped.infLoop3(Stopped.java)".equals(stackTrace[3].toString()) &&
						"test.coverage.Stopped.infLoop4(Stopped.java)".equals(stackTrace[4].toString()) &&
						"test.coverage.Stopped.infLoop(Stopped.java)" .equals(stackTrace[1].toString())
				) || (
						"test.coverage.Stopped.infLoop2(Stopped.java)".equals(stackTrace[3].toString()) &&
						"test.coverage.Stopped.infLoop3(Stopped.java)".equals(stackTrace[4].toString()) &&
						"test.coverage.Stopped.infLoop4(Stopped.java)".equals(stackTrace[1].toString()) &&
						"test.coverage.Stopped.infLoop(Stopped.java)" .equals(stackTrace[2].toString())
				) || (
						"test.coverage.Stopped.infLoop2(Stopped.java)".equals(stackTrace[4].toString()) &&
						"test.coverage.Stopped.infLoop3(Stopped.java)".equals(stackTrace[1].toString()) &&
						"test.coverage.Stopped.infLoop4(Stopped.java)".equals(stackTrace[2].toString()) &&
						"test.coverage.Stopped.infLoop(Stopped.java)" .equals(stackTrace[3].toString())
				)
		);

		assertEquals(" -- recursion.start -- ()", stackTrace[5].toString());
	}

	public void testInfLoop4b() throws Exception {
		TestCoverageStoppedCUT cut = new TestCoverageStoppedCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 4),  // 41 recursion
				new AssignPrimitive(cut.ints[1], 10), // 10 ms of delay => Test Stopped
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.infLoop, new Reference[] { cut.ints[0], cut.ints[1] } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);

		assertEquals(25.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());

		final Fault fault = faults.faults.iterator().next();
		assertNotNull(fault);
		assertEquals(fault.getExceptionName(), TestStoppedException.class.getCanonicalName());

		final StackTraceElement[] stackTrace = fault.getStackTrace();
		assertEquals(6, fault.getStackTrace().length);
		assertEquals(" --  recursion.end  -- ()", stackTrace[0].toString());
		assertEquals("test.coverage.Stopped.infLoop2(Stopped.java)", stackTrace[1].toString());
		assertEquals("test.coverage.Stopped.infLoop3(Stopped.java)", stackTrace[2].toString());
		assertEquals("test.coverage.Stopped.infLoop4(Stopped.java)", stackTrace[3].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)",  stackTrace[4].toString());
		assertEquals(" -- recursion.start -- ()", stackTrace[5].toString());
	}
}
