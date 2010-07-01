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
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.testCut.TestCoverageFaultCUT;
import testful.utils.ElementManager;

/**
 * Test for the fault detection functionality
 * @author matteo
 */
public class FaultTestCase extends GenericTestCase {

	public void testA() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.oCns, new Reference[] { }),
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a, new Reference[] { cut.objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testANull() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a, new Reference[] { cut.objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testA1() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.oCns, new Reference[] { }),
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a1, new Reference[] { cut.objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());

		Fault fault = fCov.faults.iterator().next();
		assertNotNull(fault);

		assertEquals("java.lang.NullPointerException: message", fault.getMessage());
		assertEquals(fault.getExceptionName(), UnexpectedExceptionException.class.getCanonicalName());

		assertEquals("message", fault.getCauseMessage());
		assertEquals(fault.getCauseExceptionName(), NullPointerException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Fault.a1(Fault.java)", fault.getStackTrace()[0].toString());
	}

	public void testA1Null() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a1, new Reference[] { cut.objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testA2() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.oCns, new Reference[] { }),
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a2, new Reference[] { cut.objects[0] } )
		}));


		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testA2Null() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.a2, new Reference[] { cut.objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testB() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.b , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());

		Fault fault = fCov.faults.iterator().next();
		assertNotNull(fault);

		assertEquals("java.lang.NullPointerException: message", fault.getMessage());
		assertEquals(fault.getExceptionName(), UnexpectedExceptionException.class.getCanonicalName());

		assertEquals("message", fault.getCauseMessage());
		assertEquals(fault.getCauseExceptionName(), NullPointerException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Fault.b(Fault.java)", fault.getStackTrace()[0].toString());
	}

	public void testB1() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.b1, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testC() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.c , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());

		Fault fault = fCov.faults.iterator().next();
		assertNotNull(fault);

		assertEquals("java.lang.ArithmeticException: message", fault.getMessage());
		assertEquals(fault.getExceptionName(), UnexpectedExceptionException.class.getCanonicalName());

		assertEquals("message", fault.getCauseMessage());
		assertEquals(fault.getCauseExceptionName(), ArithmeticException.class.getCanonicalName());

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Fault.c(Fault.java)", fault.getStackTrace()[0].toString());
	}

	public void testC1() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.c1, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testC2() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.c2, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testD() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.d , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testE() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.e , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());

		Fault fault = fCov.faults.iterator().next();
		assertNotNull(fault);

		assertEquals("test.coverage.MyException", fault.getMessage());
		assertEquals(fault.getExceptionName(), UnexpectedExceptionException.class.getCanonicalName());

		assertEquals(null, fault.getCauseMessage());
		assertEquals(fault.getCauseExceptionName(), "test.coverage.MyException");

		assertEquals(1, fault.getStackTrace().length);
		assertEquals("test.coverage.Fault.e(Fault.java)", fault.getStackTrace()[0].toString());
	}

	public void testE1() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cCns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.e1, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}
}
