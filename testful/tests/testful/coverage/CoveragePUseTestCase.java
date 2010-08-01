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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import testful.GenericTestCase;
import testful.coverage.whiteBox.ConditionTargetDatum;
import testful.coverage.whiteBox.ContextualId;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageBranch;
import testful.coverage.whiteBox.CoverageBranchTarget;
import testful.coverage.whiteBox.CoverageDataFlow;
import testful.coverage.whiteBox.CoveragePUse;
import testful.coverage.whiteBox.Stack;
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

	private static final Stack EMPTY_STACK = new Stack(new Integer[0]);

	private void checkDistance(Test t, Map<ConditionTargetDatum, Double> distance) throws Exception {

		for(Entry<ConditionTargetDatum, Double> entry : distance.entrySet()) {

			ElementManager<String, CoverageInformation> cov = getCoverage(t, entry.getKey());
			CoverageBranchTarget ct = (CoverageBranchTarget) cov.get(CoverageBranchTarget.KEY);

			if(entry.getValue() == null)
				assertEquals("Wrong distance on branch " + entry.getKey(), Float.NEGATIVE_INFINITY, ct.getQuality());
			else if(entry.getValue() == -1)
				assertEquals("Wrong distance on branch " + entry.getKey(), Float.POSITIVE_INFINITY, ct.getQuality());
			else
				assertEquals("Wrong distance on branch " + entry.getKey(), (float) (1.0/(entry.getValue()+0.125)), ct.getQuality());
		}

	}

	public void testFieldBase() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { })
		});
		ElementManager<String, CoverageInformation> covs = getCoverage(test);

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(10.0f, cov.getQuality());
			assertEquals("4\n5\n17\n20\n21\n24\n25\n26\n27\n31", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("0\n2\n4", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("default[]-1[]\ndefault[]-2[]\ndefault[]-3[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("0(default[])\n2(default[])\n4(default[])", cov.toString());
		}
		{
			Map<ConditionTargetDatum, Double> distance = new HashMap<ConditionTargetDatum, Double>();

			distance.put(new ConditionTargetDatum(0), -1.0);
			distance.put(new ConditionTargetDatum(0, null), -1.0);
			distance.put(new ConditionTargetDatum(0, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(0, new ContextualId(4, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(1), 10.0);
			distance.put(new ConditionTargetDatum(1, null), 10.0);
			distance.put(new ConditionTargetDatum(1, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(1, new ContextualId(4, EMPTY_STACK)), null);

			distance.put(new ConditionTargetDatum(2), -1.0);
			distance.put(new ConditionTargetDatum(2, null), -1.0);
			distance.put(new ConditionTargetDatum(2, new ContextualId(2, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(2, new ContextualId(5, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(3), 10.0);
			distance.put(new ConditionTargetDatum(3, null), 10.0);
			distance.put(new ConditionTargetDatum(3, new ContextualId(2, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(3, new ContextualId(5, EMPTY_STACK)), null);

			distance.put(new ConditionTargetDatum(4), -1.0);
			distance.put(new ConditionTargetDatum(4, null), -1.0);
			distance.put(new ConditionTargetDatum(4, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(4, new ContextualId(4, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(5), 11.0);
			distance.put(new ConditionTargetDatum(5, null), 11.0);
			distance.put(new ConditionTargetDatum(5, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(5, new ContextualId(4, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(6),  1.0);
			distance.put(new ConditionTargetDatum(6, null), 1.0);
			distance.put(new ConditionTargetDatum(6, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(6, new ContextualId(4, EMPTY_STACK)), null);

			checkDistance(test, distance);
		}
	}

	public void testFieldZero() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.zero, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		});
		ElementManager<String, CoverageInformation> covs = getCoverage(test);

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(11.0f, cov.getQuality());
			assertEquals("4\n5\n9\n17\n20\n21\n24\n25\n26\n27\n31", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("0\n2\n4", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("1[]-1[]\n1[]-3[]\n2[]-2[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("0(1[])\n2(2[])\n4(1[])", cov.toString());
		}
		{
			Map<ConditionTargetDatum, Double> distance = new HashMap<ConditionTargetDatum, Double>();

			distance.put(new ConditionTargetDatum(0), -1.0);
			distance.put(new ConditionTargetDatum(0, null), null);
			distance.put(new ConditionTargetDatum(0, new ContextualId(1, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(0, new ContextualId(4, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(1), 10.0);
			distance.put(new ConditionTargetDatum(1, null), null);
			distance.put(new ConditionTargetDatum(1, new ContextualId(1, EMPTY_STACK)), 10.0);
			distance.put(new ConditionTargetDatum(1, new ContextualId(4, EMPTY_STACK)), null);

			distance.put(new ConditionTargetDatum(2), -1.0);
			distance.put(new ConditionTargetDatum(2, null), null);
			distance.put(new ConditionTargetDatum(2, new ContextualId(2, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(2, new ContextualId(5, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(3), 10.0);
			distance.put(new ConditionTargetDatum(3, null), null);
			distance.put(new ConditionTargetDatum(3, new ContextualId(2, EMPTY_STACK)), 10.0);
			distance.put(new ConditionTargetDatum(3, new ContextualId(5, EMPTY_STACK)), null);

			distance.put(new ConditionTargetDatum(4), -1.0);
			distance.put(new ConditionTargetDatum(4, null), null);
			distance.put(new ConditionTargetDatum(4, new ContextualId(1, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(4, new ContextualId(4, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(5), 11.0);
			distance.put(new ConditionTargetDatum(5, null), null);
			distance.put(new ConditionTargetDatum(5, new ContextualId(1, EMPTY_STACK)), 11.0);
			distance.put(new ConditionTargetDatum(5, new ContextualId(4, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(6),  1.0);
			distance.put(new ConditionTargetDatum(6, null), null);
			distance.put(new ConditionTargetDatum(6, new ContextualId(1, EMPTY_STACK)),  1.0);
			distance.put(new ConditionTargetDatum(6, new ContextualId(4, EMPTY_STACK)), null);

			checkDistance(test, distance);
		}
	}

	public void testFieldSetFalse() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		});
		ElementManager<String, CoverageInformation> covs = getCoverage(test);

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(11.0f, cov.getQuality());
			assertEquals("4\n5\n13\n17\n20\n21\n24\n25\n26\n27\n31", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("0\n2\n4", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("4[]-1[]\n4[]-3[]\n5[]-2[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("0(4[])\n2(5[])\n4(4[])", cov.toString());
		}
		{
			Map<ConditionTargetDatum, Double> distance = new HashMap<ConditionTargetDatum, Double>();

			distance.put(new ConditionTargetDatum(0), -1.0);
			distance.put(new ConditionTargetDatum(0, null), null);
			distance.put(new ConditionTargetDatum(0, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(0, new ContextualId(4, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(1), 10.0);
			distance.put(new ConditionTargetDatum(1, null), null);
			distance.put(new ConditionTargetDatum(1, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(1, new ContextualId(4, EMPTY_STACK)), 10.0);

			distance.put(new ConditionTargetDatum(2), -1.0);
			distance.put(new ConditionTargetDatum(2, null), null);
			distance.put(new ConditionTargetDatum(2, new ContextualId(2, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(2, new ContextualId(5, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(3), 10.0);
			distance.put(new ConditionTargetDatum(3, null), null);
			distance.put(new ConditionTargetDatum(3, new ContextualId(2, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(3, new ContextualId(5, EMPTY_STACK)), 10.0);

			distance.put(new ConditionTargetDatum(4), -1.0);
			distance.put(new ConditionTargetDatum(4, null), null);
			distance.put(new ConditionTargetDatum(4, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(4, new ContextualId(4, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(5), 11.0);
			distance.put(new ConditionTargetDatum(5, null), null);
			distance.put(new ConditionTargetDatum(5, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(5, new ContextualId(4, EMPTY_STACK)), 11.0);
			distance.put(new ConditionTargetDatum(6),  1.0);
			distance.put(new ConditionTargetDatum(6, null), null);
			distance.put(new ConditionTargetDatum(6, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(6, new ContextualId(4, EMPTY_STACK)),  1.0);

			checkDistance(test, distance);
		}
	}

	public void testFieldFalseAll() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.zero, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		});
		ElementManager<String, CoverageInformation> covs = getCoverage(test);

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(12.0f, cov.getQuality());
			assertEquals("4\n5\n9\n13\n17\n20\n21\n24\n25\n26\n27\n31", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("0\n2\n4", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(9.0f, cov.getQuality());
			assertEquals("1[]-1[]\n1[]-3[]\n2[]-2[]\n4[]-1[]\n4[]-3[]\n5[]-2[]\ndefault[]-1[]\ndefault[]-2[]\ndefault[]-3[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(9.0f, cov.getQuality());
			assertEquals("0(1[])\n0(4[])\n0(default[])\n2(2[])\n2(5[])\n2(default[])\n4(1[])\n4(4[])\n4(default[])", cov.toString());
		}
		{
			Map<ConditionTargetDatum, Double> distance = new HashMap<ConditionTargetDatum, Double>();

			distance.put(new ConditionTargetDatum(0), -1.0);
			distance.put(new ConditionTargetDatum(0, null), -1.0);
			distance.put(new ConditionTargetDatum(0, new ContextualId(1, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(0, new ContextualId(4, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(1), 10.0);
			distance.put(new ConditionTargetDatum(1, null), 10.0);
			distance.put(new ConditionTargetDatum(1, new ContextualId(1, EMPTY_STACK)), 10.0);
			distance.put(new ConditionTargetDatum(1, new ContextualId(4, EMPTY_STACK)), 10.0);

			distance.put(new ConditionTargetDatum(2), -1.0);
			distance.put(new ConditionTargetDatum(2, null), -1.0);
			distance.put(new ConditionTargetDatum(2, new ContextualId(2, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(2, new ContextualId(5, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(3), 10.0);
			distance.put(new ConditionTargetDatum(3, null), 10.0);
			distance.put(new ConditionTargetDatum(3, new ContextualId(2, EMPTY_STACK)), 10.0);
			distance.put(new ConditionTargetDatum(3, new ContextualId(5, EMPTY_STACK)), 10.0);

			distance.put(new ConditionTargetDatum(4), -1.0);
			distance.put(new ConditionTargetDatum(4, null), -1.0);
			distance.put(new ConditionTargetDatum(4, new ContextualId(1, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(4, new ContextualId(4, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(5), 11.0);
			distance.put(new ConditionTargetDatum(5, null), 11.0);
			distance.put(new ConditionTargetDatum(5, new ContextualId(1, EMPTY_STACK)), 11.0);
			distance.put(new ConditionTargetDatum(5, new ContextualId(4, EMPTY_STACK)), 11.0);
			distance.put(new ConditionTargetDatum(6),  1.0);
			distance.put(new ConditionTargetDatum(6, null), 1.0);
			distance.put(new ConditionTargetDatum(6, new ContextualId(1, EMPTY_STACK)),  1.0);
			distance.put(new ConditionTargetDatum(6, new ContextualId(4, EMPTY_STACK)),  1.0);

			checkDistance(test, distance);
		}
	}

	public void testFieldSetTrue() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 11),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		});
		ElementManager<String, CoverageInformation> covs = getCoverage(test);

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(13.0f, cov.getQuality());
			assertEquals("4\n5\n13\n17\n18\n19\n21\n22\n23\n25\n28\n29\n31", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("1\n3\n5", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("4[]-1[]\n4[]-3[]\n5[]-2[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("1(4[])\n3(5[])\n5(4[])", cov.toString());
		}
		{
			Map<ConditionTargetDatum, Double> distance = new HashMap<ConditionTargetDatum, Double>();

			distance.put(new ConditionTargetDatum(0), 1.0);
			distance.put(new ConditionTargetDatum(0, null), null);
			distance.put(new ConditionTargetDatum(0, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(0, new ContextualId(4, EMPTY_STACK)), 1.0);
			distance.put(new ConditionTargetDatum(1), -1.0);
			distance.put(new ConditionTargetDatum(1, null), null);
			distance.put(new ConditionTargetDatum(1, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(1, new ContextualId(4, EMPTY_STACK)), -1.0);

			distance.put(new ConditionTargetDatum(2), 1.0);
			distance.put(new ConditionTargetDatum(2, null), null);
			distance.put(new ConditionTargetDatum(2, new ContextualId(2, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(2, new ContextualId(5, EMPTY_STACK)), 1.0);
			distance.put(new ConditionTargetDatum(3), -1.0);
			distance.put(new ConditionTargetDatum(3, null), null);
			distance.put(new ConditionTargetDatum(3, new ContextualId(2, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(3, new ContextualId(5, EMPTY_STACK)), -1.0);

			distance.put(new ConditionTargetDatum(4), 11.0);
			distance.put(new ConditionTargetDatum(4, null), null);
			distance.put(new ConditionTargetDatum(4, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(4, new ContextualId(4, EMPTY_STACK)), 11.0);
			distance.put(new ConditionTargetDatum(5), -1.0);
			distance.put(new ConditionTargetDatum(5, null), null);
			distance.put(new ConditionTargetDatum(5, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(5, new ContextualId(4, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(6),  1.0);
			distance.put(new ConditionTargetDatum(6, null), null);
			distance.put(new ConditionTargetDatum(6, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(6, new ContextualId(4, EMPTY_STACK)),  1.0);

			checkDistance(test, distance);
		}
	}

	public void testFieldSetBoth() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cut_cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 11),
				new Invoke(null, cut.cuts[0], cut.setA, new Reference[] { cut.ints[0] }),
				new Invoke(null, cut.cuts[0], cut.ifFieldGT10, new Reference[] { }),
		});
		ElementManager<String, CoverageInformation> covs = getCoverage(test);

		{
			CoverageBasicBlocks cov = (CoverageBasicBlocks) covs.get(CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(17.0f, cov.getQuality());
			assertEquals("4\n5\n13\n17\n18\n19\n20\n21\n22\n23\n24\n25\n26\n27\n28\n29\n31", cov.toString());
		}
		{
			CoverageBranch cov = (CoverageBranch) covs.get(CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(6.0f, cov.getQuality());
			assertEquals("0\n1\n2\n3\n4\n5", cov.toString());
		}
		{
			CoverageDataFlow cov = (CoverageDataFlow) covs.get(CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("4[]-1[]\n4[]-3[]\n5[]-2[]", cov.toString());
		}
		{
			CoveragePUse cov = (CoveragePUse) covs.get(CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(6.0f, cov.getQuality());
			assertEquals("0(4[])\n1(4[])\n2(5[])\n3(5[])\n4(4[])\n5(4[])", cov.toString());
		}
		{
			Map<ConditionTargetDatum, Double> distance = new HashMap<ConditionTargetDatum, Double>();

			distance.put(new ConditionTargetDatum(0), -1.0);
			distance.put(new ConditionTargetDatum(0, null), null);
			distance.put(new ConditionTargetDatum(0, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(0, new ContextualId(4, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(1), -1.0);
			distance.put(new ConditionTargetDatum(1, null), null);
			distance.put(new ConditionTargetDatum(1, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(1, new ContextualId(4, EMPTY_STACK)), -1.0);

			distance.put(new ConditionTargetDatum(2), -1.0);
			distance.put(new ConditionTargetDatum(2, null), null);
			distance.put(new ConditionTargetDatum(2, new ContextualId(2, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(2, new ContextualId(5, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(3), -1.0);
			distance.put(new ConditionTargetDatum(3, null), null);
			distance.put(new ConditionTargetDatum(3, new ContextualId(2, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(3, new ContextualId(5, EMPTY_STACK)), -1.0);

			distance.put(new ConditionTargetDatum(4), -1.0);
			distance.put(new ConditionTargetDatum(4, null), null);
			distance.put(new ConditionTargetDatum(4, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(4, new ContextualId(4, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(5), -1.0);
			distance.put(new ConditionTargetDatum(5, null), null);
			distance.put(new ConditionTargetDatum(5, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(5, new ContextualId(4, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(6),  1.0);
			distance.put(new ConditionTargetDatum(6, null), null);
			distance.put(new ConditionTargetDatum(6, new ContextualId(1, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(6, new ContextualId(4, EMPTY_STACK)),  1.0);

			checkDistance(test, distance);
		}
	}

	public void testParamA() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new AssignPrimitive(cut.ints[1], 0),
				new AssignPrimitive(cut.ints[2], 0),
				new Invoke(null, null, cut.ifParams, new Reference[] { cut.ints[0], cut.ints[1], cut.ints[2] }),
		});
		ElementManager<String, CoverageInformation> covs = getCoverage(test);

		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(8.0f, cov.getQuality());
			assertEquals("35\n36\n38\n39\n40\n41\n42\n44", cov.toString());
		}
		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("8\n10", cov.toString());
		}
		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(4.0f, cov.getQuality());
			assertEquals("10[]-5[]\n10[]-7[]\n9[]-4[]\n9[]-6[]", cov.toString());
		}
		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("10(10[])\n10(9[])", cov.toString());
		}
		{
			Map<ConditionTargetDatum, Double> distance = new HashMap<ConditionTargetDatum, Double>();

			distance.put(new ConditionTargetDatum(10), -1.0);
			distance.put(new ConditionTargetDatum(10, new ContextualId( 9, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(10, new ContextualId(10, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(10, new ContextualId(11, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(10, new ContextualId(12, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum( 9),  1.0);
			distance.put(new ConditionTargetDatum(9,  new ContextualId( 9, EMPTY_STACK)),  1.0);
			distance.put(new ConditionTargetDatum(9,  new ContextualId(10, EMPTY_STACK)),  1.0);
			distance.put(new ConditionTargetDatum(9,  new ContextualId(11, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(9,  new ContextualId(12, EMPTY_STACK)), null);

			checkDistance(test, distance);
		}
	}

	public void testParamA1() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 0),
				new AssignPrimitive(cut.ints[1], 1),
				new AssignPrimitive(cut.ints[2], 1),
				new Invoke(null, null, cut.ifParams, new Reference[] { cut.ints[0], cut.ints[1], cut.ints[2] }),
		});
		ElementManager<String, CoverageInformation> covs = getCoverage(test);

		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(8.0f, cov.getQuality());
			assertEquals("35\n37\n38\n39\n40\n41\n42\n44", cov.toString());
		}
		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("7\n10", cov.toString());
		}
		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(4.0f, cov.getQuality());
			assertEquals("11[]-5[]\n11[]-7[]\n12[]-4[]\n12[]-6[]", cov.toString());
		}
		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(2.0f, cov.getQuality());
			assertEquals("10(11[])\n10(12[])", cov.toString());
		}
		{
			Map<ConditionTargetDatum, Double> distance = new HashMap<ConditionTargetDatum, Double>();

			distance.put(new ConditionTargetDatum(10), -1.0);
			distance.put(new ConditionTargetDatum(10, new ContextualId( 9, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(10, new ContextualId(10, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(10, new ContextualId(11, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(10, new ContextualId(12, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum( 9),  1.0);
			distance.put(new ConditionTargetDatum(9,  new ContextualId( 9, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(9,  new ContextualId(10, EMPTY_STACK)), null);
			distance.put(new ConditionTargetDatum(9,  new ContextualId(11, EMPTY_STACK)),  1.0);
			distance.put(new ConditionTargetDatum(9,  new ContextualId(12, EMPTY_STACK)),  1.0);

			checkDistance(test, distance);
		}
	}

	public void testParamAboth() throws Exception {
		TestCoveragePUseCUT cut = new TestCoveragePUseCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 0),
				new AssignPrimitive(cut.ints[1], 1),
				new AssignPrimitive(cut.ints[2], 1),
				new Invoke(null, null, cut.ifParams, new Reference[] { cut.ints[0], cut.ints[1], cut.ints[2] }),
				new AssignPrimitive(cut.ints[0], 1),
				new AssignPrimitive(cut.ints[1], 0),
				new AssignPrimitive(cut.ints[2], 0),
				new Invoke(null, null, cut.ifParams, new Reference[] { cut.ints[0], cut.ints[1], cut.ints[2] }),
		});
		ElementManager<String, CoverageInformation> covs = getCoverage(test);

		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoverageBasicBlocks.KEY);
			assertNotNull(cov);
			assertEquals(9.0f, cov.getQuality());
			assertEquals("35\n36\n37\n38\n39\n40\n41\n42\n44", cov.toString());
		}
		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoverageBranch.KEY);
			assertNotNull(cov);
			assertEquals(3.0f, cov.getQuality());
			assertEquals("7\n8\n10", cov.toString());
		}
		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoverageDataFlow.KEY);
			assertNotNull(cov);
			assertEquals(8.0f, cov.getQuality());
			assertEquals("10[]-5[]\n10[]-7[]\n11[]-5[]\n11[]-7[]\n12[]-4[]\n12[]-6[]\n9[]-4[]\n9[]-6[]", cov.toString());
		}
		{
			CoverageInformation cov = covs.get(testful.coverage.whiteBox.CoveragePUse.KEY);
			assertNotNull(cov);
			assertEquals(4.0f, cov.getQuality());
			assertEquals("10(10[])\n10(11[])\n10(12[])\n10(9[])", cov.toString());
		}
		{
			Map<ConditionTargetDatum, Double> distance = new HashMap<ConditionTargetDatum, Double>();

			distance.put(new ConditionTargetDatum(10), -1.0);
			distance.put(new ConditionTargetDatum(10, new ContextualId( 9, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(10, new ContextualId(10, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(10, new ContextualId(11, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum(10, new ContextualId(12, EMPTY_STACK)), -1.0);
			distance.put(new ConditionTargetDatum( 9),  1.0);
			distance.put(new ConditionTargetDatum(9,  new ContextualId( 9, EMPTY_STACK)),  1.0);
			distance.put(new ConditionTargetDatum(9,  new ContextualId(10, EMPTY_STACK)),  1.0);
			distance.put(new ConditionTargetDatum(9,  new ContextualId(11, EMPTY_STACK)),  1.0);
			distance.put(new ConditionTargetDatum(9,  new ContextualId(12, EMPTY_STACK)),  1.0);

			checkDistance(test, distance);
		}
	}
}
