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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import testful.GenericTestCase;
import testful.coverage.whiteBox.ConditionTargetDatum;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageBranch;
import testful.coverage.whiteBox.CoverageBranchTarget;
import testful.model.AssignPrimitive;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.testCut.TestCoverageControlFlowCUT;
import testful.utils.ElementManager;

/**
 * Test for the control-flow coverage tracking functionality
 * @author matteo
 */
public class CoverageCFGTestCase extends GenericTestCase {

	private void checkBBCov(ElementManager<String, CoverageInformation> cov, Set<Integer> expected) {
		final CoverageBasicBlocks bbCov = (CoverageBasicBlocks) cov.get(CoverageBasicBlocks.KEY);

		assertEquals((float) expected.size(), bbCov.getQuality());

		for(Integer i : expected)
			assertTrue("basic block " + i,  bbCov.coverage.get(i));
	}

	private void checkCondCov(ElementManager<String, CoverageInformation> cov, Set<Integer> expected) {
		final CoverageBranch condCov = (CoverageBranch) cov.get(CoverageBranch.KEY);
		assertEquals((float) expected.size(), condCov.getQuality());

		for(Integer i : expected)
			assertTrue("branch " + i, condCov.coverage.get(i));
	}

	private void checkDistance(Test t, Map<Integer, Double> distance) throws Exception {

		for(int br : distance.keySet()) {
			ElementManager<String, CoverageInformation> cov = getCoverage(t, new ConditionTargetDatum(br));
			CoverageBranchTarget ct = (CoverageBranchTarget) cov.get(CoverageBranchTarget.KEY);

			assertNotNull(ct);

			if(distance.get(br) == -1)
				assertEquals("Wrong distance on branch " + br, Float.POSITIVE_INFINITY, ct.getQuality());
			else
				assertEquals("Wrong distance on branch " + br, (float) (1.0/(distance.get(br)+0.125)), ct.getQuality());
		}

	}

	public void testTracktSwitchn1() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], -1),
				new Invoke(null, cut.cuts[0], cut.tSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(93); // start method "tSwitch"
		expBB.add(101);// end method "tSwitch"

		expBr.add(38);
		expBB.add(100);

		distance.put(32, 1.0); // case: 0
		distance.put(33, 2.0); // case: 1
		distance.put(34, 3.0); // case: 2
		distance.put(35, 4.0); // case: 3
		distance.put(36, 5.0); // case: 4
		distance.put(37, 6.0); // case: 5
		distance.put(38,-1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracktSwitch0() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(null, cut.cuts[0], cut.tSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(93); // start method "tSwitch"
		expBB.add(101);// end method "tSwitch"

		expBr.add(32);
		expBB.add(94);
		expBB.add(95);
		expBB.add(96);
		expBB.add(97);

		distance.put(32,-1.0); // case: 0
		distance.put(33, 1.0); // case: 1
		distance.put(34, 2.0); // case: 2
		distance.put(35, 3.0); // case: 3
		distance.put(36, 4.0); // case: 4
		distance.put(37, 5.0); // case: 5
		distance.put(38, 1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracktSwitch1() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 1),
				new Invoke(null, cut.cuts[0], cut.tSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(93); // start method "tSwitch"
		expBB.add(101);// end method "tSwitch"

		expBr.add(33);
		expBB.add(95);
		expBB.add(96);
		expBB.add(97);

		distance.put(32, 1.0); // case: 0
		distance.put(33,-1.0); // case: 1
		distance.put(34, 1.0); // case: 2
		distance.put(35, 2.0); // case: 3
		distance.put(36, 3.0); // case: 4
		distance.put(37, 4.0); // case: 5
		distance.put(38, 2.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracktSwitch2() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 2),
				new Invoke(null, cut.cuts[0], cut.tSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(93); // start method "tSwitch"
		expBB.add(101);// end method "tSwitch"

		expBr.add(34);
		expBB.add(96);
		expBB.add(97);

		distance.put(32, 2.0); // case: 0
		distance.put(33, 1.0); // case: 1
		distance.put(34,-1.0); // case: 2
		distance.put(35, 1.0); // case: 3
		distance.put(36, 2.0); // case: 4
		distance.put(37, 3.0); // case: 5
		distance.put(38, 3.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracktSwitch3() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 3),
				new Invoke(null, cut.cuts[0], cut.tSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(93); // start method "tSwitch"
		expBB.add(101);// end method "tSwitch"
		expBr.add(35);
		expBB.add(97);

		distance.put(32, 3.0); // case: 0
		distance.put(33, 2.0); // case: 1
		distance.put(34, 1.0); // case: 2
		distance.put(35,-1.0); // case: 3
		distance.put(36, 1.0); // case: 4
		distance.put(37, 2.0); // case: 5
		distance.put(38, 3.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTracktSwitch4() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 4),
				new Invoke(null, cut.cuts[0], cut.tSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(93); // start method "tSwitch"
		expBB.add(101);// end method "tSwitch"

		expBr.add(36);
		expBB.add(98);

		distance.put(32, 4.0); // case: 0
		distance.put(33, 3.0); // case: 1
		distance.put(34, 2.0); // case: 2
		distance.put(35, 1.0); // case: 3
		distance.put(36,-1.0); // case: 4
		distance.put(37, 1.0); // case: 5
		distance.put(38, 2.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTracktSwitch5() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 5),
				new Invoke(null, cut.cuts[0], cut.tSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(93); // start method "tSwitch"
		expBB.add(101);// end method "tSwitch"

		expBr.add(37);
		expBB.add(99);

		distance.put(32, 5.0); // case: 0
		distance.put(33, 4.0); // case: 1
		distance.put(34, 3.0); // case: 2
		distance.put(35, 2.0); // case: 3
		distance.put(36, 1.0); // case: 4
		distance.put(37,-1.0); // case: 5
		distance.put(38, 1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTracktSwitch6() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 6),
				new Invoke(null, cut.cuts[0], cut.tSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(93); // start method "tSwitch"
		expBB.add(101);// end method "tSwitch"

		expBr.add(38);
		expBB.add(100);

		distance.put(32, 6.0); // case: 0
		distance.put(33, 5.0); // case: 1
		distance.put(34, 4.0); // case: 2
		distance.put(35, 3.0); // case: 3
		distance.put(36, 2.0); // case: 4
		distance.put(37, 1.0); // case: 5
		distance.put(38,-1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTrackIfObjSame() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.objif, new Reference[] { cut.cuts[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(89); // end method "ifObj"
		expBB.add(65); // method _false

		//if(i == j)
		expBB.add(81);
		expBr.add(29);
		distance.put(28, 0.0);
		distance.put(29,-1.0);
		expBB.add(82);

		//if(i != j)
		expBB.add(85);
		expBr.add(30);
		distance.put(30,-1.0);
		distance.put(31, 0.0);
		expBB.add(87);
		expBB.add(88);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTrackIfObjNotSame() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new CreateObject(cut.cuts[1], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.objif, new Reference[] { cut.cuts[1] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(89); // end method "ifObj"
		expBB.add(65); // method _false

		//if(i == j)
		expBB.add(81);
		expBr.add(28);
		distance.put(28,-1.0);
		distance.put(29, 0.0);
		expBB.add(83);
		expBB.add(84);

		//if(i != j)
		expBB.add(85);
		expBr.add(31);
		distance.put(30, 0.0);
		distance.put(31,-1.0);
		expBB.add(86);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfObjNotSameNull() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.objif, new Reference[] { cut.cuts[1] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(89); // end method "ifObj"
		expBB.add(65); // method _false

		//if(i == j)
		expBB.add(81);
		expBr.add(28);
		distance.put(28,-1.0);
		distance.put(29, 1.0);
		expBB.add(83);
		expBB.add(84);

		//if(i != j)
		expBB.add(85);
		expBr.add(31);
		distance.put(30, 1.0);
		distance.put(31,-1.0);
		expBB.add(86);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfObjAll() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.objif, new Reference[] { cut.cuts[0] }),
				new CreateObject(cut.cuts[1], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.objif, new Reference[] { cut.cuts[1] }),
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(89); // end method "ifObj"
		expBB.add(65); // method _false

		//if(i == j)
		expBB.add(81);
		expBr.add(28);
		expBr.add(29);
		distance.put(28,-1.0);
		distance.put(29,-1.0);
		expBB.add(82);
		expBB.add(83);
		expBB.add(84);

		//if(i != j)
		expBB.add(85);
		expBr.add(30);
		expBr.add(31);
		distance.put(30,-1.0);
		distance.put(31,-1.0);
		expBB.add(86);
		expBB.add(87);
		expBB.add(88);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfBoolTrue() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.bools[0], true),
				new Invoke(null, cut.cuts[0], cut.boolif, new Reference[] { cut.bools[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(77); // end method "ifBool"
		expBB.add(65); // method _false

		//if(i == true)
		expBB.add(69);
		expBr.add(25);
		distance.put(24, 1.0);
		distance.put(25,-1.0);
		expBB.add(70);

		//if(i != true)
		expBB.add(73);
		expBr.add(26);
		distance.put(26,-1.0);
		distance.put(27, 1.0);
		expBB.add(75);
		expBB.add(76);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfBoolFalse() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.bools[0], false),
				new Invoke(null, cut.cuts[0], cut.boolif, new Reference[] { cut.bools[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(77); // end method "ifBool"
		expBB.add(65); // method _false

		//if(i == true)
		expBB.add(69);
		expBr.add(24);
		distance.put(24,-1.0);
		distance.put(25, 1.0);
		expBB.add(71);
		expBB.add(72);

		//if(i != true)
		expBB.add(73);
		expBr.add(27);
		distance.put(26, 1.0);
		distance.put(27,-1.0);
		expBB.add(74);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfBoolAll() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.bools[0], true),
				new Invoke(null, cut.cuts[0], cut.boolif, new Reference[] { cut.bools[0] }),
				new AssignPrimitive(cut.bools[0], false),
				new Invoke(null, cut.cuts[0], cut.boolif, new Reference[] { cut.bools[0] }),
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(77); // end method "ifBool"
		expBB.add(65); // method _false

		//if(i == true)
		expBB.add(69);
		expBr.add(24);
		expBr.add(25);
		distance.put(24,-1.0);
		distance.put(25,-1.0);
		expBB.add(70);
		expBB.add(71);
		expBB.add(72);

		//if(i != true)
		expBB.add(73);
		expBr.add(26);
		expBr.add(27);
		distance.put(26,-1.0);
		distance.put(27,-1.0);
		expBB.add(74);
		expBB.add(75);
		expBB.add(76);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDoublen1() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.doubles[0], -1.0),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(61); // end method "ifDouble"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(37);
		expBr.add(12);
		distance.put(12,-1.0);
		distance.put(13, 1.0);
		expBB.add(39);
		expBB.add(40);

		//if(i > 1)
		expBB.add(41);
		expBr.add(14);
		distance.put(14,-1.0);
		distance.put(15, 2.0);
		expBB.add(43);
		expBB.add(44);

		//if(i <= 2)
		expBB.add(45);
		expBr.add(17);
		distance.put(16, 3.0);
		distance.put(17,-1.0);
		expBB.add(46);

		//if(i < 3)
		expBB.add(49);
		expBr.add(19);
		distance.put(18, 4.0);
		distance.put(19,-1.0);
		expBB.add(50);

		//if(i == 4)
		expBB.add(53);
		expBr.add(20);
		distance.put(20,-1.0);
		distance.put(21, 5.0);
		expBB.add(55);
		expBB.add(56);

		//if(i != 5)
		expBB.add(57);
		expBr.add(23);
		distance.put(22, 6.0);
		distance.put(23,-1.0);
		expBB.add(58);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble0() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.doubles[0], 0.0),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(61); // end method "ifDouble"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(37);
		expBr.add(13);
		distance.put(12, 0.0);
		distance.put(13,-1.0);
		expBB.add(38);

		//if(i > 1)
		expBB.add(41);
		expBr.add(14);
		distance.put(14,-1.0);
		distance.put(15, 1.0);
		expBB.add(43);
		expBB.add(44);

		//if(i <= 2)
		expBB.add(45);
		expBr.add(17);
		distance.put(16, 2.0);
		distance.put(17,-1.0);
		expBB.add(46);

		//if(i < 3)
		expBB.add(49);
		expBr.add(19);
		distance.put(18, 3.0);
		distance.put(19,-1.0);
		expBB.add(50);

		//if(i == 4)
		expBB.add(53);
		expBr.add(20);
		distance.put(20,-1.0);
		distance.put(21, 4.0);
		expBB.add(55);
		expBB.add(56);

		//if(i != 5)
		expBB.add(57);
		expBr.add(23);
		distance.put(22, 5.0);
		distance.put(23,-1.0);
		expBB.add(58);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble1() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.doubles[0], 1.0),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(61); // end method "ifDouble"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(37);
		expBr.add(13);
		distance.put(12, 1.0); //was: 2.0);
		distance.put(13,-1.0);
		expBB.add(38);

		//if(i > 1)
		expBB.add(41);
		expBr.add(14);
		distance.put(14,-1.0);
		distance.put(15, 0.0);
		expBB.add(43);
		expBB.add(44);

		//if(i <= 2)
		expBB.add(45);
		expBr.add(17);
		distance.put(16, 1.0);
		distance.put(17,-1.0);
		expBB.add(46);

		//if(i < 3)
		expBB.add(49);
		expBr.add(19);
		distance.put(18, 2.0);
		distance.put(19,-1.0);
		expBB.add(50);

		//if(i == 4)
		expBB.add(53);
		expBr.add(20);
		distance.put(20,-1.0);
		distance.put(21, 3.0);
		expBB.add(55);
		expBB.add(56);

		//if(i != 5)
		expBB.add(57);
		expBr.add(23);
		distance.put(22, 4.0);
		distance.put(23,-1.0);
		expBB.add(58);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble2() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.doubles[0], 2.0),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(61); // end method "ifDouble"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(37);
		expBr.add(13);
		distance.put(12, 2.0);
		distance.put(13,-1.0);
		expBB.add(38);

		//if(i > 1)
		expBB.add(41);
		expBr.add(15);
		distance.put(14, 1.0);
		distance.put(15,-1.0);
		expBB.add(42);

		//if(i <= 2)
		expBB.add(45);
		expBr.add(17);
		distance.put(16, 0.0);
		distance.put(17,-1.0);
		expBB.add(46);

		//if(i < 3)
		expBB.add(49);
		expBr.add(19);
		distance.put(18, 1.0);
		distance.put(19,-1.0);
		expBB.add(50);

		//if(i == 4)
		expBB.add(53);
		expBr.add(20);
		distance.put(20,-1.0);
		distance.put(21, 2.0);
		expBB.add(55);
		expBB.add(56);

		//if(i != 5)
		expBB.add(57);
		expBr.add(23);
		distance.put(22, 3.0);
		distance.put(23,-1.0);
		expBB.add(58);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble3() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.doubles[0], 3.0),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(61); // end method "ifDouble"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(37);
		expBr.add(13);
		distance.put(12, 3.0);
		distance.put(13,-1.0);
		expBB.add(38);

		//if(i > 1)
		expBB.add(41);
		expBr.add(15);
		distance.put(14, 2.0);
		distance.put(15,-1.0);
		expBB.add(42);

		//if(i <= 2)
		expBB.add(45);
		expBr.add(16);
		distance.put(16,-1.0);
		distance.put(17, 1.0);
		expBB.add(47);
		expBB.add(48);

		//if(i < 3)
		expBB.add(49);
		expBr.add(18);
		distance.put(18,-1.0);
		distance.put(19, 0.0);
		expBB.add(51);
		expBB.add(52);

		//if(i == 4)
		expBB.add(53);
		expBr.add(20);
		distance.put(20,-1.0);
		distance.put(21, 1.0);
		expBB.add(55);
		expBB.add(56);

		//if(i != 5)
		expBB.add(57);
		expBr.add(23);
		distance.put(22, 2.0);
		distance.put(23,-1.0);
		expBB.add(58);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble4() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.doubles[0], 4.0),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(61); // end method "ifDouble"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(37);
		expBr.add(13);
		distance.put(12, 4.0);
		distance.put(13,-1.0);
		expBB.add(38);

		//if(i > 1)
		expBB.add(41);
		expBr.add(15);
		distance.put(14, 3.0);
		distance.put(15,-1.0);
		expBB.add(42);

		//if(i <= 2)
		expBB.add(45);
		expBr.add(16);
		distance.put(16,-1.0);
		distance.put(17, 2.0);
		expBB.add(47);
		expBB.add(48);

		//if(i < 3)
		expBB.add(49);
		expBr.add(18);
		distance.put(18,-1.0);
		distance.put(19, 1.0);
		expBB.add(51);
		expBB.add(52);

		//if(i == 4)
		expBB.add(53);
		expBr.add(21);
		distance.put(20, 0.0);
		distance.put(21,-1.0);
		expBB.add(54);

		//if(i != 5)
		expBB.add(57);
		expBr.add(23);
		distance.put(22, 1.0);
		distance.put(23,-1.0);
		expBB.add(58);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble5() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.doubles[0], 5),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(61); // end method "ifDouble"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(37);
		expBr.add(13);
		distance.put(12, 5.0);
		distance.put(13,-1.0);
		expBB.add(38);

		//if(i > 1)
		expBB.add(41);
		expBr.add(15);
		distance.put(14, 4.0);
		distance.put(15,-1.0);
		expBB.add(42);

		//if(i <= 2)
		expBB.add(45);
		expBr.add(16);
		distance.put(16,-1.0);
		distance.put(17, 3.0);
		expBB.add(47);
		expBB.add(48);

		//if(i < 3)
		expBB.add(49);
		expBr.add(18);
		distance.put(18,-1.0);
		distance.put(19, 2.0);
		expBB.add(51);
		expBB.add(52);

		//if(i == 4)
		expBB.add(53);
		expBr.add(20);
		distance.put(20,-1.0);
		distance.put(21, 1.0);
		expBB.add(55);
		expBB.add(56);

		//if(i != 5)
		expBB.add(57);
		expBr.add(22);
		distance.put(22,-1.0);
		distance.put(23, 0.0);
		expBB.add(59);
		expBB.add(60);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDoubleAll() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.doubles[0], -1),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] }),
				new AssignPrimitive(cut.doubles[0], 0),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] }),
				new AssignPrimitive(cut.doubles[0], 1),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] }),
				new AssignPrimitive(cut.doubles[0], 2),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] }),
				new AssignPrimitive(cut.doubles[0], 3),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] }),
				new AssignPrimitive(cut.doubles[0], 4),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] }),
				new AssignPrimitive(cut.doubles[0], 5),
				new Invoke(null, cut.cuts[0], cut.doubleif, new Reference[] { cut.doubles[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(61); // end method "ifDouble"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(37);
		expBr.add(12);
		expBr.add(13);
		distance.put(12,-1.0);
		distance.put(13,-1.0);
		expBB.add(38);
		expBB.add(39);
		expBB.add(40);

		//if(i > 1)
		expBB.add(41);
		expBr.add(14);
		expBr.add(15);
		distance.put(14,-1.0);
		distance.put(15,-1.0);
		expBB.add(42);
		expBB.add(43);
		expBB.add(44);

		//if(i <= 2)
		expBB.add(45);
		expBr.add(16);
		expBr.add(17);
		distance.put(16,-1.0);
		distance.put(17,-1.0);
		expBB.add(46);
		expBB.add(47);
		expBB.add(48);

		//if(i < 3)
		expBB.add(49);
		expBr.add(18);
		expBr.add(19);
		distance.put(18,-1.0);
		distance.put(19,-1.0);
		expBB.add(50);
		expBB.add(51);
		expBB.add(52);

		//if(i == 4)
		expBB.add(53);
		expBr.add(20);
		expBr.add(21);
		distance.put(20,-1.0);
		distance.put(21,-1.0);
		expBB.add(54);
		expBB.add(55);
		expBB.add(56);


		//if(i != 5)
		expBB.add(57);
		expBr.add(22);
		expBr.add(23);
		distance.put(22,-1.0);
		distance.put(23,-1.0);
		expBB.add(58);
		expBB.add(59);
		expBB.add(60);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfIntn1() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], -1),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(33); // end method "ifInt"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(9);
		expBr.add(0);
		distance.put(0,-1.0);
		distance.put(1, 1.0);
		expBB.add(11);
		expBB.add(12);

		//if(i > 1)
		expBB.add(13);
		expBr.add(2);
		distance.put(2,-1.0);
		distance.put(3, 2.0);
		expBB.add(15);
		expBB.add(16);

		//if(i <= 2)
		expBB.add(17);
		expBr.add(5);
		distance.put(4, 3.0);
		distance.put(5,-1.0);
		expBB.add(18);

		//if(i < 3)
		expBB.add(21);
		expBr.add(7);
		distance.put(6, 4.0);
		distance.put(7,-1.0);
		expBB.add(22);

		//if(i == 4)
		expBB.add(25);
		expBr.add(8);
		distance.put(8,-1.0);
		distance.put(9, 5.0);
		expBB.add(27);
		expBB.add(28);

		//if(i != 5)
		expBB.add(29);
		expBr.add(11);
		distance.put(10, 6.0);
		distance.put(11,-1.0);
		expBB.add(30);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt0() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 0),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(33); // end method "ifInt"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(9);
		expBr.add(1);
		distance.put(0, 0.0);
		distance.put(1,-1.0);
		expBB.add(10);

		//if(i > 1)
		expBB.add(13);
		expBr.add(2);
		distance.put(2,-1.0);
		distance.put(3, 1.0);
		expBB.add(15);
		expBB.add(16);

		//if(i <= 2)
		expBB.add(17);
		expBr.add(5);
		distance.put(4, 2.0);
		distance.put(5,-1.0);
		expBB.add(18);

		//if(i < 3)
		expBB.add(21);
		expBr.add(7);
		distance.put(6, 3.0);
		distance.put(7,-1.0);
		expBB.add(22);

		//if(i == 4)
		expBB.add(25);
		expBr.add(8);
		distance.put(8,-1.0);
		distance.put(9, 4.0);
		expBB.add(27);
		expBB.add(28);

		//if(i != 5)
		expBB.add(29);
		expBr.add(11);
		distance.put(10, 5.0);
		distance.put(11,-1.0);
		expBB.add(30);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt1() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(33); // end method "ifInt"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(9);
		expBr.add(1);
		distance.put(0, 1.0);
		distance.put(1,-1.0);
		expBB.add(10);

		//if(i > 1)
		expBB.add(13);
		expBr.add(2);
		distance.put(2,-1.0);
		distance.put(3, 0.0);
		expBB.add(15);
		expBB.add(16);

		//if(i <= 2)
		expBB.add(17);
		expBr.add(5);
		distance.put(4, 1.0);
		distance.put(5,-1.0);
		expBB.add(18);

		//if(i < 3)
		expBB.add(21);
		expBr.add(7);
		distance.put(6, 2.0);
		distance.put(7,-1.0);
		expBB.add(22);

		//if(i == 4)
		expBB.add(25);
		expBr.add(8);
		distance.put(8,-1.0);
		distance.put(9, 3.0);
		expBB.add(27);
		expBB.add(28);

		//if(i != 5)
		expBB.add(29);
		expBr.add(11);
		distance.put(10, 4.0);
		distance.put(11,-1.0);
		expBB.add(30);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt2() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 2),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(33); // end method "ifInt"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(9);
		expBr.add(1);
		distance.put(0, 2.0);
		distance.put(1,-1.0);
		expBB.add(10);

		//if(i > 1)
		expBB.add(13);
		expBr.add(3);
		distance.put(2, 1.0);
		distance.put(3,-1.0);
		expBB.add(14);

		//if(i <= 2)
		expBB.add(17);
		expBr.add(5);
		distance.put(4, 0.0);
		distance.put(5,-1.0);
		expBB.add(18);

		//if(i < 3)
		expBB.add(21);
		expBr.add(7);
		distance.put(6, 1.0);
		distance.put(7,-1.0);
		expBB.add(22);

		//if(i == 4)
		expBB.add(25);
		expBr.add(8);
		distance.put(8,-1.0);
		distance.put(9, 2.0);
		expBB.add(27);
		expBB.add(28);

		//if(i != 5)
		expBB.add(29);
		expBr.add(11);
		distance.put(10, 3.0);
		distance.put(11,-1.0);
		expBB.add(30);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt3() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 3),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(33); // end method "ifInt"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(9);
		expBr.add(1);
		distance.put(0, 3.0);
		distance.put(1,-1.0);
		expBB.add(10);

		//if(i > 1)
		expBB.add(13);
		expBr.add(3);
		distance.put(2, 2.0);
		distance.put(3,-1.0);
		expBB.add(14);

		//if(i <= 2)
		expBB.add(17);
		expBr.add(4);
		distance.put(4,-1.0);
		distance.put(5, 1.0);
		expBB.add(19);
		expBB.add(20);

		//if(i < 3)
		expBB.add(21);
		expBr.add(6);
		distance.put(6,-1.0);
		distance.put(7, 0.0);
		expBB.add(23);
		expBB.add(24);

		//if(i == 4)
		expBB.add(25);
		expBr.add(8);
		distance.put(8,-1.0);
		distance.put(9, 1.0);
		expBB.add(27);
		expBB.add(28);

		//if(i != 5)
		expBB.add(29);
		expBr.add(11);
		distance.put(10, 2.0);
		distance.put(11,-1.0);
		expBB.add(30);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt4() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 4),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(33); // end method "ifInt"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(9);
		expBr.add(1);
		distance.put(0, 4.0);
		distance.put(1,-1.0);
		expBB.add(10);

		//if(i > 1)
		expBB.add(13);
		expBr.add(3);
		distance.put(2, 3.0);
		distance.put(3,-1.0);
		expBB.add(14);

		//if(i <= 2)
		expBB.add(17);
		expBr.add(4);
		distance.put(4,-1.0);
		distance.put(5, 2.0);
		expBB.add(19);
		expBB.add(20);

		//if(i < 3)
		expBB.add(21);
		expBr.add(6);
		distance.put(6,-1.0);
		distance.put(7, 1.0);
		expBB.add(23);
		expBB.add(24);

		//if(i == 4)
		expBB.add(25);
		expBr.add(9);
		distance.put(8, 0.0);
		distance.put(9,-1.0);
		expBB.add(26);

		//if(i != 5)
		expBB.add(29);
		expBr.add(11);
		distance.put(10, 1.0);
		distance.put(11,-1.0);
		expBB.add(30);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt5() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 5),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(33); // end method "ifInt"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(9);
		expBr.add(1);
		distance.put(0, 5.0);
		distance.put(1,-1.0);
		expBB.add(10);

		//if(i > 1)
		expBB.add(13);
		expBr.add(3);
		distance.put(2, 4.0);
		distance.put(3,-1.0);
		expBB.add(14);

		//if(i <= 2)
		expBB.add(17);
		expBr.add(4);
		distance.put(4,-1.0);
		distance.put(5, 3.0);
		expBB.add(19);
		expBB.add(20);

		//if(i < 3)
		expBB.add(21);
		expBr.add(6);
		distance.put(6,-1.0);
		distance.put(7, 2.0);
		expBB.add(23);
		expBB.add(24);

		//if(i == 4)
		expBB.add(25);
		expBr.add(8);
		distance.put(8,-1.0);
		distance.put(9, 1.0);
		expBB.add(27);
		expBB.add(28);

		//if(i != 5)
		expBB.add(29);
		expBr.add(10);
		distance.put(10,-1.0);
		distance.put(11, 0.0);
		expBB.add(31);
		expBB.add(32);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfIntAll() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], -1),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] }),
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] }),
				new AssignPrimitive(cut.ints[0], 1),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] }),
				new AssignPrimitive(cut.ints[0], 2),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] }),
				new AssignPrimitive(cut.ints[0], 3),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] }),
				new AssignPrimitive(cut.ints[0], 4),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] }),
				new AssignPrimitive(cut.ints[0], 5),
				new Invoke(null, cut.cuts[0], cut.intif, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(33); // end method "ifInt"
		expBB.add(65); // method _false

		//if(i >= 0)
		expBB.add(9);
		expBr.add(0);
		expBr.add(1);
		distance.put(0,-1.0);
		distance.put(1,-1.0);
		expBB.add(10);
		expBB.add(11);
		expBB.add(12);

		//if(i > 1)
		expBB.add(13);
		expBr.add(2);
		expBr.add(3);
		distance.put(2,-1.0);
		distance.put(3,-1.0);
		expBB.add(14);
		expBB.add(15);
		expBB.add(16);

		//if(i <= 2)
		expBB.add(17);
		expBr.add(4);
		expBr.add(5);
		distance.put(4,-1.0);
		distance.put(5,-1.0);
		expBB.add(18);
		expBB.add(19);
		expBB.add(20);

		//if(i < 3)
		expBB.add(21);
		expBr.add(6);
		expBr.add(7);
		distance.put(6,-1.0);
		distance.put(7,-1.0);
		expBB.add(22);
		expBB.add(23);
		expBB.add(24);

		//if(i == 4)
		expBB.add(25);
		expBr.add(8);
		expBr.add(9);
		distance.put(8,-1.0);
		distance.put(9,-1.0);
		expBB.add(26);
		expBB.add(27);
		expBB.add(28);


		//if(i != 5)
		expBB.add(29);
		expBr.add(10);
		expBr.add(11);
		distance.put(10,-1.0);
		distance.put(11,-1.0);
		expBB.add(30);
		expBB.add(31);
		expBB.add(32);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitchn1() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], -1),
				new Invoke(null, cut.cuts[0], cut.lSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(105); // start method "tSwitch"
		expBB.add(113);// end method "tSwitch"

		expBr.add(45);
		expBB.add(112);

		distance.put(39,  1.0); // case: 0
		distance.put(40, 11.0); // case: 10
		distance.put(41, 21.0); // case: 20
		distance.put(42, 31.0); // case: 30
		distance.put(43, 41.0); // case: 40
		distance.put(44, 51.0); // case: 50
		distance.put(45, -1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch0() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(null, cut.cuts[0], cut.lSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(105); // start method "tSwitch"
		expBB.add(113);// end method "tSwitch"

		expBr.add(39);
		expBB.add(106);
		expBB.add(107);
		expBB.add(108);
		expBB.add(109);

		distance.put(39, -1.0); // case: 0
		distance.put(40, 10.0); // case: 10
		distance.put(41, 20.0); // case: 20
		distance.put(42, 30.0); // case: 30
		distance.put(43, 40.0); // case: 40
		distance.put(44, 50.0); // case: 50
		distance.put(45,  1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch10() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 10),
				new Invoke(null, cut.cuts[0], cut.lSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(105); // start method "tSwitch"
		expBB.add(113);// end method "tSwitch"

		expBr.add(40);
		expBB.add(107);
		expBB.add(108);
		expBB.add(109);

		distance.put(39, 10.0); // case: 0
		distance.put(40, -1.0); // case: 10
		distance.put(41, 10.0); // case: 20
		distance.put(42, 20.0); // case: 30
		distance.put(43, 30.0); // case: 40
		distance.put(44, 40.0); // case: 50
		distance.put(45,  1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch20() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 20),
				new Invoke(null, cut.cuts[0], cut.lSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(105); // start method "tSwitch"
		expBB.add(113);// end method "tSwitch"

		expBr.add(41);
		expBB.add(108);
		expBB.add(109);

		distance.put(39, 20.0); // case: 0
		distance.put(40, 10.0); // case: 10
		distance.put(41, -1.0); // case: 20
		distance.put(42, 10.0); // case: 30
		distance.put(43, 20.0); // case: 40
		distance.put(44, 30.0); // case: 50
		distance.put(45,  1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch30() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 30),
				new Invoke(null, cut.cuts[0], cut.lSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(105); // start method "tSwitch"
		expBB.add(113);// end method "tSwitch"

		expBr.add(42);
		expBB.add(109);

		distance.put(39, 30.0); // case: 0
		distance.put(40, 20.0); // case: 10
		distance.put(41, 10.0); // case: 20
		distance.put(42, -1.0); // case: 30
		distance.put(43, 10.0); // case: 40
		distance.put(44, 20.0); // case: 50
		distance.put(45,  1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch40() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 40),
				new Invoke(null, cut.cuts[0], cut.lSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(105); // start method "tSwitch"
		expBB.add(113);// end method "tSwitch"

		expBr.add(43);
		expBB.add(110);

		distance.put(39, 40.0); // case: 0
		distance.put(40, 30.0); // case: 10
		distance.put(41, 20.0); // case: 20
		distance.put(42, 10.0); // case: 30
		distance.put(43, -1.0); // case: 40
		distance.put(44, 10.0); // case: 50
		distance.put(45,  1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch50() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 50),
				new Invoke(null, cut.cuts[0], cut.lSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(105); // start method "tSwitch"
		expBB.add(113);// end method "tSwitch"

		expBr.add(44);
		expBB.add(111);

		distance.put(39, 50.0); // case: 0
		distance.put(40, 40.0); // case: 10
		distance.put(41, 30.0); // case: 20
		distance.put(42, 20.0); // case: 30
		distance.put(43, 10.0); // case: 40
		distance.put(44, -1.0); // case: 50
		distance.put(45,  1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch51() throws Exception {
		TestCoverageControlFlowCUT cut = new TestCoverageControlFlowCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new AssignPrimitive(cut.ints[0], 51),
				new Invoke(null, cut.cuts[0], cut.lSwitch, new Reference[] { cut.ints[0] })
		});

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(5);  // constructor
		expBB.add(105); // start method "tSwitch"
		expBB.add(113);// end method "tSwitch"

		expBr.add(45);
		expBB.add(112);

		distance.put(39, 51.0); // case: 0
		distance.put(40, 41.0); // case: 10
		distance.put(41, 31.0); // case: 20
		distance.put(42, 21.0); // case: 30
		distance.put(43, 11.0); // case: 40
		distance.put(44,  1.0); // case: 50
		distance.put(45, -1.0); // default

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}
}
