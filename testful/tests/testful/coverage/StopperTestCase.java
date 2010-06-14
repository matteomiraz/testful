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

import testful.coverage.fault.FaultsCoverage;
import testful.coverage.stopper.TestStoppedException;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.utils.ElementManager;

/**
 * Test for the stopper functionality
 * @author matteo
 */
public class StopperTestCase extends testful.StopperTestCase {

	public void testLongMethod1() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod1, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(5.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}

	public void testLongMethod1b() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod1, new Reference[] { } ),
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(8.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}

	public void testLongMethod2() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod2, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(5.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}

	public void testLongMethod2b() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod2, new Reference[] { } ),
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(8.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}

	public void testLongMethod3() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod3, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(5.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}

	public void testLongMethod3b() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod3, new Reference[] { } ),
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(8.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}

	public void testLongMethod4() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod4, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(5.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}

	public void testLongMethod4b() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod4, new Reference[] { } ),
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(8.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}

	public void testLongMethod5() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod5, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(7.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}

	public void testLongMethod5b() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod5, new Reference[] { } ),
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], execute, new Reference[] { })
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		assertEquals(10.0f, covs.get(CoverageBasicBlocks.KEY).getQuality());

		FaultsCoverage faults = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(faults);
		assertEquals(1.0f, faults.getQuality());
		assertTrue(faults.faults.iterator().next().getFault() instanceof TestStoppedException);
	}
}
