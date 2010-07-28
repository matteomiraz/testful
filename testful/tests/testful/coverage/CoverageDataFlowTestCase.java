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
import testful.coverage.whiteBox.CoverageBranch;
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

	/** Checks that the du-pairs tracking is disabled: The complete control-flow graph coverage ensures the complete data-flow graph coverage */
	public void testDataFlowlocal1Def2UsesTrue() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], true),
				new Invoke(null, null, cut.local1Def2Uses, new Reference[] { cut.bools[0] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(0.0f, cov.getQuality());
			assertEquals("", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("1", cov.toString());
		}

	}

	/** Checks that the du-pairs tracking is disabled: The complete control-flow graph coverage ensures the complete data-flow graph coverage */
	public void testDataFlowlocal1Def2UsesFalse() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], false),
				new Invoke(null, null, cut.local1Def2Uses, new Reference[] { cut.bools[0] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(0.0f, cov.getQuality());
			assertEquals("", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("0", cov.toString());
		}
	}

	/** Checks that the du-pairs tracking is disabled: The complete control-flow graph coverage ensures the complete data-flow graph coverage */
	public void testDataFlowlocal1Def2UsesBoth() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], false),
				new Invoke(null, null, cut.local1Def2Uses, new Reference[] { cut.bools[0] }),
				new AssignPrimitive(cut.bools[0], true),
				new Invoke(null, null, cut.local1Def2Uses, new Reference[] { cut.bools[0] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(0.0f, cov.getQuality());
			assertEquals("", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("0\n1", cov.toString());
		}
	}

	/** Checks that the du-pairs tracking is enabled */
	public void testDataFlowlocal2Defs1UseTrue() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], true),
				new Invoke(null, null, cut.local2Defs1Use, new Reference[] { cut.bools[0] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("4[]-9[]", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("3", cov.toString());
		}

	}

	/** Checks that the du-pairs tracking is enabled */
	public void testDataFlowlocal2Defs1UseFalse() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], false),
				new Invoke(null, null, cut.local2Defs1Use, new Reference[] { cut.bools[0] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("5[]-9[]", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("2", cov.toString());
		}
	}

	/** Checks that the du-pairs tracking is enabled */
	public void testDataFlowlocal2Defs1UseBoth() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], false),
				new Invoke(null, null, cut.local2Defs1Use, new Reference[] { cut.bools[0] }),
				new AssignPrimitive(cut.bools[0], true),
				new Invoke(null, null, cut.local2Defs1Use, new Reference[] { cut.bools[0] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("4[]-9[]\n5[]-9[]", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("2\n3", cov.toString());
		}
	}

	/** Checks that the du-pairs tracking is enabled */
	public void testDataFlowlocal2Defs2UsesTrueTrue() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], true),
				new AssignPrimitive(cut.bools[1], true),
				new Invoke(null, null, cut.local2Defs2Uses, new Reference[] { cut.bools[0], cut.bools[1] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("6[]-12[]", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("5\n7", cov.toString());
		}

	}

	/** Checks that the du-pairs tracking is enabled */
	public void testDataFlowlocal2Defs2UsesTrueFalse() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], true),
				new AssignPrimitive(cut.bools[1], false),
				new Invoke(null, null, cut.local2Defs2Uses, new Reference[] { cut.bools[0], cut.bools[1] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("6[]-13[]", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("5\n6", cov.toString());
		}

	}

	/** Checks that the du-pairs tracking is enabled */
	public void testDataFlowlocal2Defs2UsesFalseTrue() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], false),
				new AssignPrimitive(cut.bools[1], true),
				new Invoke(null, null, cut.local2Defs2Uses, new Reference[] { cut.bools[0], cut.bools[1] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("7[]-12[]", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("4\n7", cov.toString());
		}

	}

	/** Checks that the du-pairs tracking is enabled */
	public void testDataFlowlocal2Defs2UsesFalseFalse() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], false),
				new AssignPrimitive(cut.bools[1], false),
				new Invoke(null, null, cut.local2Defs2Uses, new Reference[] { cut.bools[0], cut.bools[1] })
		}));

		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("7[]-13[]", cov.toString());
		}

		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("4\n6", cov.toString());
		}

	}

	public void testDataFlow() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(0.0f, cov.getQuality());
		assertEquals("", cov.toString());
	}

	public void testDataFlowBase() throws Exception {
		TestCoverageDataFlowCUT cut = new TestCoverageDataFlowCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(1.0f, cov.getQuality());
		assertEquals("1[]-2[]", cov.toString());
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
		assertEquals(2.0f, cov.getQuality());
		assertEquals("1[]-2[]\ndefault[]-1[]", cov.toString());
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
		assertEquals(2.0f, cov.getQuality());
		assertEquals("1[]-2[]\n2[]-1[]", cov.toString());
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
		assertEquals(3.0f, cov.getQuality());
		assertEquals("1[]-2[]\n2[]-1[]\ndefault[]-1[]", cov.toString());
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
		assertEquals(3.0f, cov.getQuality());
		assertEquals("1[]-2[]\n1[]-3[]\ndefault[]-14[]", cov.toString());
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
		assertEquals(2.0f, cov.getQuality());
		assertEquals("1[]-2[]\ndefault[]-14[]", cov.toString());
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
		assertEquals(3.0f, cov.getQuality());
		assertEquals("1[]-2[]\n1[]-3[]\ndefault[]-14[]", cov.toString());
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
		assertEquals(4.0f, cov.getQuality());
		assertEquals("1[]-2[]\n1[]-3[]\n1[]-4[]\n8[]-14[]", cov.toString());
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
		assertEquals(3.0f, cov.getQuality());
		assertEquals("1[]-2[]\n1[]-4[]\n8[]-14[]", cov.toString());
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
		assertEquals(4.0f, cov.getQuality());
		assertEquals("1[]-2[]\n1[]-3[]\n1[]-4[]\n8[]-14[]", cov.toString());
	}

	public void testDataFlowCtx() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(0.0f, cov.getQuality());
		assertEquals("", cov.toString());
	}

	public void testDataFlowCtxBase() throws Exception {
		TestCoverageDataFlowCtxCUT cut = new TestCoverageDataFlowCtxCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(cut.inners[0], cut.cuts[0], cut.getI, new Reference[] { }),
		}));

		CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
		assertNotNull(cov);
		assertEquals(1.0f, cov.getQuality());
		assertEquals("1[1]-2[16]", cov.toString());
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
		assertEquals(2.0f, cov.getQuality());
		assertEquals("1[1]-2[16]\ndefault[]-1[8]", cov.toString());
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
		assertEquals(2.0f, cov.getQuality());
		assertEquals("1[1]-2[16]\n2[12]-1[8]", cov.toString());
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
		assertEquals(3.0f, cov.getQuality());
		assertEquals("1[1]-2[16]\n2[12]-1[8]\ndefault[]-1[8]", cov.toString());
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
		assertEquals(3.0f, cov.getQuality());
		assertEquals("1[1]-2[16]\n1[1]-3[24]\ndefault[]-10[24, 66]", cov.toString());
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
		assertEquals(2.0f, cov.getQuality());
		assertEquals("1[1]-2[16]\ndefault[]-10[66]", cov.toString());
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
		assertEquals(4.0f, cov.getQuality());
		assertEquals("1[1]-2[16]\n1[1]-3[24]\ndefault[]-10[24, 66]\ndefault[]-10[66]", cov.toString());
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
		assertEquals(4.0f, cov.getQuality());
		assertEquals("1[1]-2[16]\n1[1]-3[24]\n1[1]-4[30]\n6[30, 70]-10[24, 66]", cov.toString());
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
		assertEquals(3.0f, cov.getQuality());
		assertEquals("1[1]-2[16]\n1[1]-4[30]\n6[30, 70]-10[66]", cov.toString());
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
		assertEquals(5.0f, cov.getQuality());
		assertEquals("1[1]-2[16]\n1[1]-3[24]\n1[1]-4[30]\n6[30, 70]-10[24, 66]\n6[30, 70]-10[66]", cov.toString());
	}
}
