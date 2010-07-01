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

		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod1(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(2, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod1(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(2, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod2(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(2, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod2(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(2, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod3(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(2, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod3(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(2, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod4(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(2, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod4(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(2, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod5(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(2, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.longMethod5(Stopped.java)", fault.getStackTrace()[1].toString());
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

		assertEquals(1024, fault.getStackTrace().length);
		assertTrue(
				"testful.coverage.whiteBox.TrackerWhiteBox.getTracker(TrackerWhiteBox.java:35)".equals(fault.getStackTrace()[0].toString()) ||
				"test.coverage.Stopped.infLoop(Stopped.java)".equals(fault.getStackTrace()[0].toString())
		);
		for(int i = 1; i < 1024; i++ )
			assertEquals("wrong " + i + " stack trace element" , "test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[i].toString());

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

		assertEquals(51, fault.getStackTrace().length);
		assertEquals("testful.coverage.stopper.TestStoppedException.check(TestStoppedException.java:49)", fault.getStackTrace()[0].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[1].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[2].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[3].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[4].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[5].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[6].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[7].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[8].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[9].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[10].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[11].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[12].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[13].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[14].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[15].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[16].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[17].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[18].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[19].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[20].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[21].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[22].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[23].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[24].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[25].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[26].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[27].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[28].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[29].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[30].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[31].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[32].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[33].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[34].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[35].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[36].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[37].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[38].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[39].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[40].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[41].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[42].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[43].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[44].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[45].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[46].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[47].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[48].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[49].toString());
		assertEquals("test.coverage.Stopped.infLoop(Stopped.java)", fault.getStackTrace()[50].toString());
	}
}
