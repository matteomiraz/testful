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
import testful.testCut.TestCoverageDataFlowCtxCUT;
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
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(4.0f, cov.getQuality());
		assertEquals("1[]-1[]\n1[]-2[]\n2[]-6[]\n6[]-7[]", cov.toString());
	}

	public void testDataFlowA1() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(6.0f, cov.getQuality());
		assertEquals("1[]-1[]\n1[]-2[]\n2[]-6[]\n3[]-4[]\n6[]-7[]\ndefault[]-3[]", cov.toString());
	}

	public void testDataFlowA2() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(7.0f, cov.getQuality());
		assertEquals("1[]-1[]\n1[]-2[]\n2[]-6[]\n3[]-4[]\n4[]-5[]\n5[]-3[]\n6[]-7[]", cov.toString());
	}

	public void testDataFlowA3() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(8.0f, cov.getQuality());
		assertEquals("1[]-1[]\n1[]-2[]\n2[]-6[]\n3[]-4[]\n4[]-5[]\n5[]-3[]\n6[]-7[]\ndefault[]-3[]", cov.toString());
	}

	public void testDataFlowIA1() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(9.0f, cov.getQuality());
		assertEquals("10[]-11[]\n1[]-1[]\n1[]-2[]\n20[]-25[]\n2[]-6[]\n2[]-9[]\n6[]-7[]\n9[]-10[]\ndefault[]-24[]", cov.toString());
	}

	public void testDataFlowIA1d() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.inners[0], cut.i_getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(6.0f, cov.getQuality());
		assertEquals("1[]-1[]\n1[]-2[]\n20[]-25[]\n2[]-6[]\n6[]-7[]\ndefault[]-24[]", cov.toString());
	}

	public void testDataFlowIA1b() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.getIA, new Reference[] { }),
				new Invoke(null, cut.inners[0], cut.i_getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(9.0f, cov.getQuality());
		assertEquals("10[]-11[]\n1[]-1[]\n1[]-2[]\n20[]-25[]\n2[]-6[]\n2[]-9[]\n6[]-7[]\n9[]-10[]\ndefault[]-24[]", cov.toString());
	}

	public void testDataFlowIA2() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.setIA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(13.0f, cov.getQuality());
		assertEquals("10[]-11[]\n11[]-14[]\n12[]-13[]\n1[]-1[]\n1[]-2[]\n20[]-25[]\n21[]-26[]\n22[]-24[]\n2[]-12[]\n2[]-6[]\n2[]-9[]\n6[]-7[]\n9[]-10[]", cov.toString());
	}

	public void testDataFlowIA2d() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.setIA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.inners[0], cut.i_getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(10.0f, cov.getQuality());
		assertEquals("11[]-14[]\n12[]-13[]\n1[]-1[]\n1[]-2[]\n20[]-25[]\n21[]-26[]\n22[]-24[]\n2[]-12[]\n2[]-6[]\n6[]-7[]", cov.toString());
	}

	public void testDataFlowIA2b() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.setIA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getIA, new Reference[] { }),
				new Invoke(null, cut.inners[0], cut.i_getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(13.0f, cov.getQuality());
		assertEquals("10[]-11[]\n11[]-14[]\n12[]-13[]\n1[]-1[]\n1[]-2[]\n20[]-25[]\n21[]-26[]\n22[]-24[]\n2[]-12[]\n2[]-6[]\n2[]-9[]\n6[]-7[]\n9[]-10[]", cov.toString());
	}

	public void testDataFlowCtxBase() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(4.0f, cov.getQuality());
		assertEquals("1[1]-1[1]\n1[1]-2[1]\n2[1]-6[16]\n6[16]-7[16]", cov.toString());
	}

	public void testDataFlowCtxA1() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(6.0f, cov.getQuality());
		assertEquals("1[1]-1[1]\n1[1]-2[1]\n2[1]-6[16]\n3[8]-4[8]\n6[16]-7[16]\ndefault[]-3[8]", cov.toString());
	}

	public void testDataFlowCtxA2() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(7.0f, cov.getQuality());
		assertEquals("1[1]-1[1]\n1[1]-2[1]\n2[1]-6[16]\n3[8]-4[8]\n4[12]-5[12]\n5[12]-3[8]\n6[16]-7[16]", cov.toString());
	}

	public void testDataFlowCtxA3() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(8.0f, cov.getQuality());
		assertEquals("1[1]-1[1]\n1[1]-2[1]\n2[1]-6[16]\n3[8]-4[8]\n4[12]-5[12]\n5[12]-3[8]\n6[16]-7[16]\ndefault[]-3[8]", cov.toString());
	}

	public void testDataFlowCtxIA1() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(9.0f, cov.getQuality());
		assertEquals("10[24]-11[24]\n1[1]-1[1]\n1[1]-2[1]\n20[24, 66]-25[24, 66]\n2[1]-6[16]\n2[1]-9[24]\n6[16]-7[16]\n9[24]-10[24]\ndefault[]-24[24, 66]", cov.toString());
	}

	public void testDataFlowCtxIA1d() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.inners[0], cut.i_getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(6.0f, cov.getQuality());
		assertEquals("1[1]-1[1]\n1[1]-2[1]\n20[66]-25[66]\n2[1]-6[16]\n6[16]-7[16]\ndefault[]-24[66]", cov.toString());
	}

	public void testDataFlowCtxIA1b() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.getIA, new Reference[] { }),
				new Invoke(null, cut.inners[0], cut.i_getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(11.0f, cov.getQuality());
		assertEquals("10[24]-11[24]\n1[1]-1[1]\n1[1]-2[1]\n20[24, 66]-25[24, 66]\n20[66]-25[66]\n2[1]-6[16]\n2[1]-9[24]\n6[16]-7[16]\n9[24]-10[24]\ndefault[]-24[24, 66]\ndefault[]-24[66]", cov.toString());
	}

	public void testDataFlowCtxIA2() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.setIA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(13.0f, cov.getQuality());
		assertEquals("10[24]-11[24]\n11[30]-14[30]\n12[30]-13[30]\n1[1]-1[1]\n1[1]-2[1]\n20[24, 66]-25[24, 66]\n21[30, 70]-26[30, 70]\n22[30, 70]-24[24, 66]\n2[1]-12[30]\n2[1]-6[16]\n2[1]-9[24]\n6[16]-7[16]\n9[24]-10[24]", cov.toString());
	}

	public void testDataFlowCtxIA2d() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.setIA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.inners[0], cut.i_getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(10.0f, cov.getQuality());
		assertEquals("11[30]-14[30]\n12[30]-13[30]\n1[1]-1[1]\n1[1]-2[1]\n20[66]-25[66]\n21[30, 70]-26[30, 70]\n22[30, 70]-24[66]\n2[1]-12[30]\n2[1]-6[16]\n6[16]-7[16]", cov.toString());
	}

	public void testDataFlowCtxIA2b() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.setIA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.getIA, new Reference[] { }),
				new Invoke(null, cut.inners[0], cut.i_getIA, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(15.0f, cov.getQuality());
		assertEquals("10[24]-11[24]\n11[30]-14[30]\n12[30]-13[30]\n1[1]-1[1]\n1[1]-2[1]\n20[24, 66]-25[24, 66]\n20[66]-25[66]\n21[30, 70]-26[30, 70]\n22[30, 70]-24[24, 66]\n22[30, 70]-24[66]\n2[1]-12[30]\n2[1]-6[16]\n2[1]-9[24]\n6[16]-7[16]\n9[24]-10[24]", cov.toString());
	}
}
