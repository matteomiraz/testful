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
import testful.coverage.whiteBox.CoverageDataFlow;
import testful.model.AssignPrimitive;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.testCut.TestCoverageDataFlowCUT;
import testful.utils.ElementManager;

/**
 * Test for the data-flow coverage tracking functionality
 * @author matteo
 */
public class CoverageDataFlowTestCase extends GenericTestCase {

	public void testDataFlowBase() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(2.0f, cov.getQuality());
		assertEquals("66[]-93[]\n66[]-94[]", cov.toString());
	}

	public void testDataFlowA1() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(4.0f, cov.getQuality());
		assertEquals("66[]-93[]\n66[]-94[]\n68[]-96[]\ndefault[]-95[]", cov.toString());
	}

	public void testDataFlowA2() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(5.0f, cov.getQuality());
		assertEquals("66[]-93[]\n66[]-94[]\n68[]-96[]\n69[]-97[]\n70[]-95[]", cov.toString());
	}

	public void testDataFlowA3() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(6.0f, cov.getQuality());
		assertEquals("66[]-93[]\n66[]-94[]\n68[]-96[]\n69[]-97[]\n70[]-95[]\ndefault[]-95[]", cov.toString());
	}

}
