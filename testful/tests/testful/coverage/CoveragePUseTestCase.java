/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
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
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageBranch;
import testful.coverage.whiteBox.CoverageDataFlow;
import testful.coverage.whiteBox.CoveragePUse;
import testful.model.AssignPrimitive;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.testCut.TestCoveragePUseCUT;
import testful.utils.ElementManager;

/**
 * Testing the p-use coverage
 * @author matteo
 */
public class CoveragePUseTestCase extends GenericTestCase {

	public void testBase() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { })
		}));

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(5.0f, cov.getQuality());
			assertEquals("4\n5\n17\n20\n21", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("0", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("default[]-1[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("0(default[])", cov.toString());
		}
	}

	public void testZero() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.zero, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		}));

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(6.0f, cov.getQuality());
			assertEquals("4\n5\n9\n17\n20\n21", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("0", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("1[]-1[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("0(1[])", cov.toString());
		}
	}

	public void testSetFalse() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		}));

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(6.0f, cov.getQuality());
			assertEquals("4\n5\n13\n17\n20\n21", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("0", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("3[]-1[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("0(3[])", cov.toString());
		}
	}

	public void testFalseAll() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.zero, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		}));

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(7.0f, cov.getQuality());
			assertEquals("4\n5\n9\n13\n17\n20\n21", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("0", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("1[]-1[]\n3[]-1[]\ndefault[]-1[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("0(1[])\n0(3[])\n0(default[])", cov.toString());
		}
	}

	public void testSetTrue() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 11),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		}));

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(7.0f, cov.getQuality());
			assertEquals("4\n5\n13\n17\n18\n19\n21", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("1", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("3[]-1[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("1(3[])", cov.toString());
		}
	}

	public void testSetBoth() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 11),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		}));

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(8.0f, cov.getQuality());
			assertEquals("4\n5\n13\n17\n18\n19\n20\n21", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("0\n1", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(1.0f, cov.getQuality());
			assertEquals("3[]-1[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("0(3[])\n1(3[])", cov.toString());
		}
	}



}
