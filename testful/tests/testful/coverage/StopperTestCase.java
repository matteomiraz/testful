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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
		assertTrue(faults.faults.iterator().next().getExceptionName().equals(TestStoppedException.class.getCanonicalName()));
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
	}
}
