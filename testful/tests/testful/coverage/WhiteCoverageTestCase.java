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

import testful.ConfigCut;
import testful.GenericTestCase;
import testful.coverage.whiteBox.ConditionTargetDatum;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageConditionTarget;
import testful.coverage.whiteBox.CoverageConditions;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Methodz;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;


public class WhiteCoverageTestCase extends GenericTestCase {

	private TestCluster cluster;
	private ReferenceFactory refFactory;

	private Reference c0;
	private Reference c1;
	private Reference i0;
	private Reference b0;
	private Reference d0;

	private Constructorz cns;

	private Methodz intif;
	private Methodz doubleif;
	private Methodz boolif;
	private Methodz objif;
	private Methodz tSwitch;
	private Methodz lSwitch;

	@Override
	protected void setUp() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.WhiteSample");
		cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		refFactory = new ReferenceFactory(cluster, 2, 1);

		Clazz cut = cluster.getCut();

		Clazz iClazz = null;
		Clazz bClazz = null;
		Clazz dClazz = null;
		for(Clazz clazz : cluster.getCluster()) {
			if("java.lang.Boolean".equals(clazz.getClassName())) bClazz = clazz;
			if("java.lang.Integer".equals(clazz.getClassName())) iClazz = clazz;
			if("java.lang.Double".equals(clazz.getClassName()))  dClazz = clazz;
		}

		assertNotNull("cannot find boolean class", bClazz);
		assertNotNull("cannot find integer class", iClazz);
		assertNotNull("cannot find double class",  dClazz);

		c0 = refFactory.getReferences(cut)[0];
		c1 = refFactory.getReferences(cut)[1];
		i0 = refFactory.getReferences(iClazz)[0];
		b0 = refFactory.getReferences(bClazz)[0];
		d0 = refFactory.getReferences(dClazz)[0];

		cns = cut.getConstructors()[0];

		intif = null;
		doubleif = null;
		boolif = null;
		objif = null;
		tSwitch = null;
		lSwitch = null;
		for(Methodz m : cut.getMethods()) {
			if("intif".equals(m.getName())) intif = m;
			if("doubleif".equals(m.getName())) doubleif = m;
			if("boolif".equals(m.getName())) boolif = m;
			if("objif".equals(m.getName())) objif = m;
			if("tSwitch".equals(m.getName())) tSwitch = m;
			if("lSwitch".equals(m.getName())) lSwitch = m;
		}

		assertNotNull("method not found", intif);
		assertNotNull("method not found", doubleif);
		assertNotNull("method not found", boolif);
		assertNotNull("method not found", objif);
		assertNotNull("method not found", tSwitch);
		assertNotNull("method not found", lSwitch);
	}

	@Override
	protected void tearDown() throws Exception {
		cluster = null;
		refFactory = null;

		c0 = null;
		c1 = null;
		i0 = null;
		b0 = null;
		d0 = null;

		cns = null;

		intif = null;
		doubleif = null;
		boolif = null;
		objif = null;
		tSwitch = null;
		lSwitch = null;
	}

	private void checkBBCov(ElementManager<String, CoverageInformation> cov, Set<Integer> expected) {
		final CoverageBasicBlocks bbCov = (CoverageBasicBlocks) cov.get(CoverageBasicBlocks.KEY_CODE);

		assertEquals((float) expected.size(), bbCov.getQuality());

		for(Integer i : expected)
			assertTrue(bbCov.coverage.get(i));
	}

	private void checkCondCov(ElementManager<String, CoverageInformation> cov, Set<Integer> expected) {
		final CoverageConditions condCov = (CoverageConditions) cov.get(CoverageConditions.KEY_CODE);
		assertEquals((float) expected.size(), condCov.getQuality());

		for(Integer i : expected)
			assertTrue(condCov.coverage.get(i));
	}

	private void checkDistance(Test t, Map<Integer, Double> distance) throws Exception {

		for(int br : distance.keySet()) {
			ElementManager<String, CoverageInformation> cov = getCoverage(t, new ConditionTargetDatum(br));
			CoverageConditionTarget ct = (CoverageConditionTarget) cov.get(CoverageConditionTarget.getKEY(br));

			assertEquals("Wrong distance on branch " + br, (float) (1.0f/distance.get(br)), ct.getQuality());
		}

	}

	public void testTracktSwitchn1() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, -1),
				new Invoke(null, c0, tSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(92); // start method "tSwitch"
		expBB.add(100);// end method "tSwitch"

		expBr.add(38);
		expBB.add(99);

		distance.put(32, 1.0); // case: 0
		distance.put(33, 2.0); // case: 1
		distance.put(34, 3.0); // case: 2
		distance.put(35, 4.0); // case: 3
		distance.put(36, 5.0); // case: 4
		distance.put(37, 6.0); // case: 5
		distance.put(38, 0.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracktSwitch0() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 0),
				new Invoke(null, c0, tSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(92); // start method "tSwitch"
		expBB.add(100);// end method "tSwitch"

		expBr.add(32);
		expBB.add(93);
		expBB.add(94);
		expBB.add(95);
		expBB.add(96);

		distance.put(32, 0.0); // case: 0
		distance.put(33, 1.0); // case: 1
		distance.put(34, 2.0); // case: 2
		distance.put(35, 3.0); // case: 3
		distance.put(36, 4.0); // case: 4
		distance.put(37, 5.0); // case: 5
		distance.put(38, 1.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracktSwitch1() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 1),
				new Invoke(null, c0, tSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(92); // start method "tSwitch"
		expBB.add(100);// end method "tSwitch"

		expBr.add(33);
		expBB.add(94);
		expBB.add(95);
		expBB.add(96);

		distance.put(32, 1.0); // case: 0
		distance.put(33, 0.0); // case: 1
		distance.put(34, 1.0); // case: 2
		distance.put(35, 2.0); // case: 3
		distance.put(36, 3.0); // case: 4
		distance.put(37, 4.0); // case: 5
		distance.put(38, 2.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracktSwitch2() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 2),
				new Invoke(null, c0, tSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(92); // start method "tSwitch"
		expBB.add(100);// end method "tSwitch"

		expBr.add(34);
		expBB.add(95);
		expBB.add(96);

		distance.put(32, 2.0); // case: 0
		distance.put(33, 1.0); // case: 1
		distance.put(34, 0.0); // case: 2
		distance.put(35, 1.0); // case: 3
		distance.put(36, 2.0); // case: 4
		distance.put(37, 3.0); // case: 5
		distance.put(38, 3.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracktSwitch3() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 3),
				new Invoke(null, c0, tSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(92); // start method "tSwitch"
		expBB.add(100);// end method "tSwitch"

		expBr.add(35);
		expBB.add(96);

		distance.put(32, 3.0); // case: 0
		distance.put(33, 2.0); // case: 1
		distance.put(34, 1.0); // case: 2
		distance.put(35, 0.0); // case: 3
		distance.put(36, 1.0); // case: 4
		distance.put(37, 2.0); // case: 5
		distance.put(38, 3.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTracktSwitch4() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 4),
				new Invoke(null, c0, tSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(92); // start method "tSwitch"
		expBB.add(100);// end method "tSwitch"

		expBr.add(36);
		expBB.add(97);

		distance.put(32, 4.0); // case: 0
		distance.put(33, 3.0); // case: 1
		distance.put(34, 2.0); // case: 2
		distance.put(35, 1.0); // case: 3
		distance.put(36, 0.0); // case: 4
		distance.put(37, 1.0); // case: 5
		distance.put(38, 2.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTracktSwitch5() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 5),
				new Invoke(null, c0, tSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(92); // start method "tSwitch"
		expBB.add(100);// end method "tSwitch"

		expBr.add(37);
		expBB.add(98);

		distance.put(32, 5.0); // case: 0
		distance.put(33, 4.0); // case: 1
		distance.put(34, 3.0); // case: 2
		distance.put(35, 2.0); // case: 3
		distance.put(36, 1.0); // case: 4
		distance.put(37, 0.0); // case: 5
		distance.put(38, 1.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTracktSwitch6() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 6),
				new Invoke(null, c0, tSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(92); // start method "tSwitch"
		expBB.add(100);// end method "tSwitch"

		expBr.add(38);
		expBB.add(99);

		distance.put(32, 6.0); // case: 0
		distance.put(33, 5.0); // case: 1
		distance.put(34, 4.0); // case: 2
		distance.put(35, 3.0); // case: 3
		distance.put(36, 2.0); // case: 4
		distance.put(37, 1.0); // case: 5
		distance.put(38, 0.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTrackIfObjSame() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, objif, new Reference[] { c0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(88); // end method "ifObj"
		expBB.add(64); // method _false

		//if(i == j)
		expBB.add(80);
		expBr.add(29);
		distance.put(28, 1.0);
		distance.put(29, 0.0);
		expBB.add(81);

		//if(i != j)
		expBB.add(84);
		expBr.add(30);
		distance.put(30, 0.0);
		distance.put(31, 1.0);
		expBB.add(86);
		expBB.add(87);

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTrackIfObjNotSame() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new CreateObject(c1, cns, new Reference[] { }),
				new Invoke(null, c0, objif, new Reference[] { c1 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(88); // end method "ifObj"
		expBB.add(64); // method _false

		//if(i == j)
		expBB.add(80);
		expBr.add(28);
		distance.put(28, 0.0);
		distance.put(29, 1.0);
		expBB.add(82);
		expBB.add(83);

		//if(i != j)
		expBB.add(84);
		expBr.add(31);
		distance.put(30, 1.0);
		distance.put(31, 0.0);
		expBB.add(85);

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfObjNotSameNull() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, objif, new Reference[] { c1 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(88); // end method "ifObj"
		expBB.add(64); // method _false

		//if(i == j)
		expBB.add(80);
		expBr.add(28);
		distance.put(28, 0.0);
		distance.put(29, 2.0);
		expBB.add(82);
		expBB.add(83);

		//if(i != j)
		expBB.add(84);
		expBr.add(31);
		distance.put(30, 2.0);
		distance.put(31, 0.0);
		expBB.add(85);

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfObjAll() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, objif, new Reference[] { c0 }),
				new CreateObject(c1, cns, new Reference[] { }),
				new Invoke(null, c0, objif, new Reference[] { c1 }),
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(88); // end method "ifObj"
		expBB.add(64); // method _false

		//if(i == j)
		expBB.add(80);
		expBr.add(28);
		expBr.add(29);
		distance.put(28, 0.0);
		distance.put(29, 0.0);
		expBB.add(81);
		expBB.add(82);
		expBB.add(83);

		//if(i != j)
		expBB.add(84);
		expBr.add(30);
		expBr.add(31);
		distance.put(30, 0.0);
		distance.put(31, 0.0);
		expBB.add(85);
		expBB.add(86);
		expBB.add(87);

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}



	public void testTrackIfBoolTrue() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(b0, true),
				new Invoke(null, c0, boolif, new Reference[] { b0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(76); // end method "ifBool"
		expBB.add(64); // method _false

		//if(i == true)
		expBB.add(68);
		expBr.add(25);
		distance.put(24, 1.0);
		distance.put(25, 0.0);
		expBB.add(69);

		//if(i != true)
		expBB.add(72);
		expBr.add(26);
		distance.put(26, 0.0);
		distance.put(27, 1.0);
		expBB.add(74);
		expBB.add(75);

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTrackIfBoolFalse() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(b0, false),
				new Invoke(null, c0, boolif, new Reference[] { b0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(76); // end method "ifBool"
		expBB.add(64); // method _false

		//if(i == true)
		expBB.add(68);
		expBr.add(24);
		distance.put(24, 0.0);
		distance.put(25, 1.0);
		expBB.add(70);
		expBB.add(71);

		//if(i != true)
		expBB.add(72);
		expBr.add(27);
		distance.put(26, 1.0);
		distance.put(27, 0.0);
		expBB.add(73);

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTrackIfBoolAll() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(b0, true),
				new Invoke(null, c0, boolif, new Reference[] { b0 }),
				new AssignPrimitive(b0, false),
				new Invoke(null, c0, boolif, new Reference[] { b0 }),
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(76); // end method "ifBool"
		expBB.add(64); // method _false

		//if(i == true)
		expBB.add(68);
		expBr.add(24);
		expBr.add(25);
		distance.put(24, 0.0);
		distance.put(25, 0.0);
		expBB.add(69);
		expBB.add(70);
		expBB.add(71);

		//if(i != true)
		expBB.add(72);
		expBr.add(26);
		expBr.add(27);
		distance.put(26, 0.0);
		distance.put(27, 0.0);
		expBB.add(73);
		expBB.add(74);
		expBB.add(75);

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}


	public void testTrackIfDoublen1() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(d0, -1.0),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, doubleif, new Reference[] { d0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(60); // end method "ifDouble"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(36);
		expBr.add(12);
		distance.put(12, 0.0);
		distance.put(13, 2.0);
		expBB.add(38);
		expBB.add(39);

		//if(i > 1)
		expBB.add(40);
		expBr.add(14);
		distance.put(14, 0.0);
		distance.put(15, 2.0); //was: 3.0
		expBB.add(42);
		expBB.add(43);

		//if(i <= 2)
		expBB.add(44);
		expBr.add(17);
		distance.put(16,  2.0); //was: 4.0
		distance.put(17,  0.0);
		expBB.add(45);

		//if(i < 3)
		expBB.add(48);
		expBr.add(19);
		distance.put(18,  2.0); //was: 5.0
		distance.put(19,  0.0);
		expBB.add(49);

		//if(i == 4)
		expBB.add(52);
		expBr.add(20);
		distance.put(20,  0.0);
		distance.put(21,  2.0); //was: 6.0
		expBB.add(54);
		expBB.add(55);

		//if(i != 5)
		expBB.add(56);
		expBr.add(23);
		distance.put(22, 2.0); //was: 7.0
		distance.put(23, 0.0);
		expBB.add(57);

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble0() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(d0, 0.0),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, doubleif, new Reference[] { d0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(60); // end method "ifDouble"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(36);
		expBr.add(13);
		distance.put(12, 1.0);
		distance.put(13, 0.0);
		expBB.add(37);

		//if(i > 1)
		expBB.add(40);
		expBr.add(14);
		distance.put(14, 0.0);
		distance.put(15, 2.0); //was:  2.0);
		expBB.add(42);
		expBB.add(43);

		//if(i <= 2)
		expBB.add(44);
		expBr.add(17);
		distance.put(16, 2.0); //was:  3.0);
		distance.put(17,  0.0);
		expBB.add(45);

		//if(i < 3)
		expBB.add(48);
		expBr.add(19);
		distance.put(18, 2.0); //was:  4.0);
		distance.put(19,  0.0);
		expBB.add(49);

		//if(i == 4)
		expBB.add(52);
		expBr.add(20);
		distance.put(20,  0.0);
		distance.put(21, 2.0); //was:  5.0);
		expBB.add(54);
		expBB.add(55);

		//if(i != 5)
		expBB.add(56);
		expBr.add(23);
		distance.put(22, 2.0); //was: 6.0);
		distance.put(23, 0.0);
		expBB.add(57);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble1() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(d0, 1.0),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, doubleif, new Reference[] { d0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(60); // end method "ifDouble"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(36);
		expBr.add(13);
		distance.put(12, 2.0); //was: 2.0);
		distance.put(13, 0.0);
		expBB.add(37);

		//if(i > 1)
		expBB.add(40);
		expBr.add(14);
		distance.put(14, 0.0);
		distance.put(15, 1.0);
		expBB.add(42);
		expBB.add(43);

		//if(i <= 2)
		expBB.add(44);
		expBr.add(17);
		distance.put(16, 2.0); //was: 2.0);
		distance.put(17,  0.0);
		expBB.add(45);

		//if(i < 3)
		expBB.add(48);
		expBr.add(19);
		distance.put(18, 2.0); //was: 3.0);
		distance.put(19,  0.0);
		expBB.add(49);

		//if(i == 4)
		expBB.add(52);
		expBr.add(20);
		distance.put(20,  0.0);
		distance.put(21, 2.0); //was: 4.0);
		expBB.add(54);
		expBB.add(55);

		//if(i != 5)
		expBB.add(56);
		expBr.add(23);
		distance.put(22, 2.0); //was: 5.0);
		distance.put(23, 0.0);
		expBB.add(57);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble2() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(d0, 2.0),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, doubleif, new Reference[] { d0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(60); // end method "ifDouble"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(36);
		expBr.add(13);
		distance.put(12, 2.0); //was: 3.0);
		distance.put(13, 0.0);
		expBB.add(37);

		//if(i > 1)
		expBB.add(40);
		expBr.add(15);
		distance.put(14, 2.0);
		distance.put(15, 0.0);
		expBB.add(41);

		//if(i <= 2)
		expBB.add(44);
		expBr.add(17);
		distance.put(16, 1.0);
		distance.put(17,  0.0);
		expBB.add(45);

		//if(i < 3)
		expBB.add(48);
		expBr.add(19);
		distance.put(18, 2.0); //was: 2.0);
		distance.put(19,  0.0);
		expBB.add(49);

		//if(i == 4)
		expBB.add(52);
		expBr.add(20);
		distance.put(20,  0.0);
		distance.put(21, 2.0); //was: 3.0);
		expBB.add(54);
		expBB.add(55);

		//if(i != 5)
		expBB.add(56);
		expBr.add(23);
		distance.put(22, 2.0); //was: 4.0);
		distance.put(23, 0.0);
		expBB.add(57);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble3() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(d0, 3.0),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, doubleif, new Reference[] { d0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(60); // end method "ifDouble"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(36);
		expBr.add(13);
		distance.put(12, 2.0); //was:4.0);
		distance.put(13, 0.0);
		expBB.add(37);

		//if(i > 1)
		expBB.add(40);
		expBr.add(15);
		distance.put(14, 2.0); //was: 3.0);
		distance.put(15,  0.0);
		expBB.add(41);

		//if(i <= 2)
		expBB.add(44);
		expBr.add(16);
		distance.put(16,  0.0);
		distance.put(17,  2.0);
		expBB.add(46);
		expBB.add(47);

		//if(i < 3)
		expBB.add(48);
		expBr.add(18);
		distance.put(18,  0.0);
		distance.put(19,  1.0);
		expBB.add(50);
		expBB.add(51);

		//if(i == 4)
		expBB.add(52);
		expBr.add(20);
		distance.put(20,  0.0);
		distance.put(21,  2.0);
		expBB.add(54);
		expBB.add(55);

		//if(i != 5)
		expBB.add(56);
		expBr.add(23);
		distance.put(22, 2.0); //was: 3.0);
		distance.put(23, 0.0);
		expBB.add(57);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble4() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(d0, 4.0),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, doubleif, new Reference[] { d0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(60); // end method "ifDouble"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(36);
		expBr.add(13);
		distance.put(12, 2.0); //was: 5.0);
		distance.put(13, 0.0);
		expBB.add(37);

		//if(i > 1)
		expBB.add(40);
		expBr.add(15);
		distance.put(14, 2.0); //was: 4.0);
		distance.put(15,  0.0);
		expBB.add(41);

		//if(i <= 2)
		expBB.add(44);
		expBr.add(16);
		distance.put(16,  0.0);
		distance.put(17,  2.0); //was: 3.0);
		expBB.add(46);
		expBB.add(47);

		//if(i < 3)
		expBB.add(48);
		expBr.add(18);
		distance.put(18,  0.0);
		distance.put(19,  2.0);
		expBB.add(50);
		expBB.add(51);

		//if(i == 4)
		expBB.add(52);
		expBr.add(21);
		distance.put(20,  1.0);
		distance.put(21,  0.0);
		expBB.add(53);

		//if(i != 5)
		expBB.add(56);
		expBr.add(23);
		distance.put(22, 2.0);
		distance.put(23, 0.0);
		expBB.add(57);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDouble5() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(d0, 5),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, doubleif, new Reference[] { d0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(60); // end method "ifDouble"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(36);
		expBr.add(13);
		distance.put(12, 2.0); //was: 6.0);
		distance.put(13, 0.0);
		expBB.add(37);

		//if(i > 1)
		expBB.add(40);
		expBr.add(15);
		distance.put(14, 2.0); //was: 5.0);
		distance.put(15,  0.0);
		expBB.add(41);

		//if(i <= 2)
		expBB.add(44);
		expBr.add(16);
		distance.put(16,  0.0);
		distance.put(17, 2.0); //was: 4.0);
		expBB.add(46);
		expBB.add(47);

		//if(i < 3)
		expBB.add(48);
		expBr.add(18);
		distance.put(18,  0.0);
		distance.put(19, 2.0); //was: 3.0);
		expBB.add(50);
		expBB.add(51);

		//if(i == 4)
		expBB.add(52);
		expBr.add(20);
		distance.put(20,  0.0);
		distance.put(21,  2.0);
		expBB.add(54);
		expBB.add(55);

		//if(i != 5)
		expBB.add(56);
		expBr.add(22);
		distance.put(22, 0.0);
		distance.put(23, 1.0);
		expBB.add(58);
		expBB.add(59);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfDoubleAll() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(d0, -1),
				new Invoke(null, c0, doubleif, new Reference[] { d0 }),
				new AssignPrimitive(d0, 0),
				new Invoke(null, c0, doubleif, new Reference[] { d0 }),
				new AssignPrimitive(d0, 1),
				new Invoke(null, c0, doubleif, new Reference[] { d0 }),
				new AssignPrimitive(d0, 2),
				new Invoke(null, c0, doubleif, new Reference[] { d0 }),
				new AssignPrimitive(d0, 3),
				new Invoke(null, c0, doubleif, new Reference[] { d0 }),
				new AssignPrimitive(d0, 4),
				new Invoke(null, c0, doubleif, new Reference[] { d0 }),
				new AssignPrimitive(d0, 5),
				new Invoke(null, c0, doubleif, new Reference[] { d0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(60); // end method "ifDouble"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(36);
		expBr.add(12);
		expBr.add(13);
		distance.put(12, 0.0);
		distance.put(13, 0.0);
		expBB.add(37);
		expBB.add(38);
		expBB.add(39);

		//if(i > 1)
		expBB.add(40);
		expBr.add(14);
		expBr.add(15);
		distance.put(14, 0.0);
		distance.put(15,  0.0);
		expBB.add(41);
		expBB.add(42);
		expBB.add(43);

		//if(i <= 2)
		expBB.add(44);
		expBr.add(16);
		expBr.add(17);
		distance.put(16,  0.0);
		distance.put(17,  0.0);
		expBB.add(45);
		expBB.add(46);
		expBB.add(47);

		//if(i < 3)
		expBB.add(48);
		expBr.add(18);
		expBr.add(19);
		distance.put(18,  0.0);
		distance.put(19,  0.0);
		expBB.add(49);
		expBB.add(50);
		expBB.add(51);

		//if(i == 4)
		expBB.add(52);
		expBr.add(20);
		expBr.add(21);
		distance.put(20,  0.0);
		distance.put(21,  0.0);
		expBB.add(53);
		expBB.add(54);
		expBB.add(55);


		//if(i != 5)
		expBB.add(56);
		expBr.add(22);
		expBr.add(23);
		distance.put(22, 0.0);
		distance.put(23, 0.0);
		expBB.add(57);
		expBB.add(58);
		expBB.add(59);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfIntn1() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, -1),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, intif, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(32); // end method "ifInt"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(8);
		expBr.add(0);
		distance.put(0, 0.0);
		distance.put(1, 2.0);
		expBB.add(10);
		expBB.add(11);

		//if(i > 1)
		expBB.add(12);
		expBr.add(2);
		distance.put(2, 0.0);
		distance.put(3, 3.0);
		expBB.add(14);
		expBB.add(15);

		//if(i <= 2)
		expBB.add(16);
		expBr.add(5);
		distance.put(4, 4.0);
		distance.put(5, 0.0);
		expBB.add(17);

		//if(i < 3)
		expBB.add(20);
		expBr.add(7);
		distance.put(6, 5.0);
		distance.put(7, 0.0);
		expBB.add(21);

		//if(i == 4)
		expBB.add(24);
		expBr.add(8);
		distance.put(8, 0.0);
		distance.put(9, 6.0);
		expBB.add(26);
		expBB.add(27);

		//if(i != 5)
		expBB.add(28);
		expBr.add(11);
		distance.put(10, 7.0);
		distance.put(11, 0.0);
		expBB.add(29);

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt0() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 0),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, intif, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(32); // end method "ifInt"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(8);
		expBr.add(1);
		distance.put(0, 1.0);
		distance.put(1, 0.0);
		expBB.add(9);

		//if(i > 1)
		expBB.add(12);
		expBr.add(2);
		distance.put(2, 0.0);
		distance.put(3, 2.0);
		expBB.add(14);
		expBB.add(15);

		//if(i <= 2)
		expBB.add(16);
		expBr.add(5);
		distance.put(4, 3.0);
		distance.put(5, 0.0);
		expBB.add(17);

		//if(i < 3)
		expBB.add(20);
		expBr.add(7);
		distance.put(6, 4.0);
		distance.put(7, 0.0);
		expBB.add(21);

		//if(i == 4)
		expBB.add(24);
		expBr.add(8);
		distance.put(8, 0.0);
		distance.put(9, 5.0);
		expBB.add(26);
		expBB.add(27);

		//if(i != 5)
		expBB.add(28);
		expBr.add(11);
		distance.put(10, 6.0);
		distance.put(11, 0.0);
		expBB.add(29);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt1() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 1),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, intif, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(32); // end method "ifInt"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(8);
		expBr.add(1);
		distance.put(0, 2.0);
		distance.put(1, 0.0);
		expBB.add(9);

		//if(i > 1)
		expBB.add(12);
		expBr.add(2);
		distance.put(2, 0.0);
		distance.put(3, 1.0);
		expBB.add(14);
		expBB.add(15);

		//if(i <= 2)
		expBB.add(16);
		expBr.add(5);
		distance.put(4, 2.0);
		distance.put(5, 0.0);
		expBB.add(17);

		//if(i < 3)
		expBB.add(20);
		expBr.add(7);
		distance.put(6, 3.0);
		distance.put(7, 0.0);
		expBB.add(21);

		//if(i == 4)
		expBB.add(24);
		expBr.add(8);
		distance.put(8, 0.0);
		distance.put(9, 4.0);
		expBB.add(26);
		expBB.add(27);

		//if(i != 5)
		expBB.add(28);
		expBr.add(11);
		distance.put(10, 5.0);
		distance.put(11, 0.0);
		expBB.add(29);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt2() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 2),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, intif, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(32); // end method "ifInt"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(8);
		expBr.add(1);
		distance.put(0, 3.0);
		distance.put(1, 0.0);
		expBB.add(9);

		//if(i > 1)
		expBB.add(12);
		expBr.add(3);
		distance.put(2, 2.0);
		distance.put(3, 0.0);
		expBB.add(13);

		//if(i <= 2)
		expBB.add(16);
		expBr.add(5);
		distance.put(4, 1.0);
		distance.put(5, 0.0);
		expBB.add(17);

		//if(i < 3)
		expBB.add(20);
		expBr.add(7);
		distance.put(6, 2.0);
		distance.put(7, 0.0);
		expBB.add(21);

		//if(i == 4)
		expBB.add(24);
		expBr.add(8);
		distance.put(8, 0.0);
		distance.put(9, 3.0);
		expBB.add(26);
		expBB.add(27);

		//if(i != 5)
		expBB.add(28);
		expBr.add(11);
		distance.put(10, 4.0);
		distance.put(11, 0.0);
		expBB.add(29);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt3() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 3),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, intif, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(32); // end method "ifInt"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(8);
		expBr.add(1);
		distance.put(0, 4.0);
		distance.put(1, 0.0);
		expBB.add(9);

		//if(i > 1)
		expBB.add(12);
		expBr.add(3);
		distance.put(2, 3.0);
		distance.put(3, 0.0);
		expBB.add(13);

		//if(i <= 2)
		expBB.add(16);
		expBr.add(4);
		distance.put(4, 0.0);
		distance.put(5, 2.0);
		expBB.add(18);
		expBB.add(19);

		//if(i < 3)
		expBB.add(20);
		expBr.add(6);
		distance.put(6, 0.0);
		distance.put(7, 1.0);
		expBB.add(22);
		expBB.add(23);

		//if(i == 4)
		expBB.add(24);
		expBr.add(8);
		distance.put(8, 0.0);
		distance.put(9, 2.0);
		expBB.add(26);
		expBB.add(27);

		//if(i != 5)
		expBB.add(28);
		expBr.add(11);
		distance.put(10, 3.0);
		distance.put(11, 0.0);
		expBB.add(29);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt4() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 4),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, intif, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(32); // end method "ifInt"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(8);
		expBr.add(1);
		distance.put(0, 5.0);
		distance.put(1, 0.0);
		expBB.add(9);

		//if(i > 1)
		expBB.add(12);
		expBr.add(3);
		distance.put(2, 4.0);
		distance.put(3, 0.0);
		expBB.add(13);

		//if(i <= 2)
		expBB.add(16);
		expBr.add(4);
		distance.put(4, 0.0);
		distance.put(5, 3.0);
		expBB.add(18);
		expBB.add(19);

		//if(i < 3)
		expBB.add(20);
		expBr.add(6);
		distance.put(6, 0.0);
		distance.put(7, 2.0);
		expBB.add(22);
		expBB.add(23);

		//if(i == 4)
		expBB.add(24);
		expBr.add(9);
		distance.put(8, 1.0);
		distance.put(9, 0.0);
		expBB.add(25);

		//if(i != 5)
		expBB.add(28);
		expBr.add(11);
		distance.put(10, 2.0);
		distance.put(11, 0.0);
		expBB.add(29);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfInt5() throws Exception {
		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 5),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, intif, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(32); // end method "ifInt"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(8);
		expBr.add(1);
		distance.put(0, 6.0);
		distance.put(1, 0.0);
		expBB.add(9);

		//if(i > 1)
		expBB.add(12);
		expBr.add(3);
		distance.put(2, 5.0);
		distance.put(3, 0.0);
		expBB.add(13);

		//if(i <= 2)
		expBB.add(16);
		expBr.add(4);
		distance.put(4, 0.0);
		distance.put(5, 4.0);
		expBB.add(18);
		expBB.add(19);

		//if(i < 3)
		expBB.add(20);
		expBr.add(6);
		distance.put(6, 0.0);
		distance.put(7, 3.0);
		expBB.add(22);
		expBB.add(23);

		//if(i == 4)
		expBB.add(24);
		expBr.add(8);
		distance.put(8, 0.0);
		distance.put(9, 2.0);
		expBB.add(26);
		expBB.add(27);

		//if(i != 5)
		expBB.add(28);
		expBr.add(10);
		distance.put(10, 0.0);
		distance.put(11, 1.0);
		expBB.add(30);
		expBB.add(31);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTrackIfIntAll() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, -1),
				new Invoke(null, c0, intif, new Reference[] { i0 }),
				new AssignPrimitive(i0, 0),
				new Invoke(null, c0, intif, new Reference[] { i0 }),
				new AssignPrimitive(i0, 1),
				new Invoke(null, c0, intif, new Reference[] { i0 }),
				new AssignPrimitive(i0, 2),
				new Invoke(null, c0, intif, new Reference[] { i0 }),
				new AssignPrimitive(i0, 3),
				new Invoke(null, c0, intif, new Reference[] { i0 }),
				new AssignPrimitive(i0, 4),
				new Invoke(null, c0, intif, new Reference[] { i0 }),
				new AssignPrimitive(i0, 5),
				new Invoke(null, c0, intif, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(32); // end method "ifInt"
		expBB.add(64); // method _false

		//if(i >= 0)
		expBB.add(8);
		expBr.add(0);
		expBr.add(1);
		distance.put(0, 0.0);
		distance.put(1, 0.0);
		expBB.add(9);
		expBB.add(10);
		expBB.add(11);

		//if(i > 1)
		expBB.add(12);
		expBr.add(2);
		expBr.add(3);
		distance.put(2, 0.0);
		distance.put(3, 0.0);
		expBB.add(13);
		expBB.add(14);
		expBB.add(15);

		//if(i <= 2)
		expBB.add(16);
		expBr.add(4);
		expBr.add(5);
		distance.put(4, 0.0);
		distance.put(5, 0.0);
		expBB.add(17);
		expBB.add(18);
		expBB.add(19);

		//if(i < 3)
		expBB.add(20);
		expBr.add(6);
		expBr.add(7);
		distance.put(6, 0.0);
		distance.put(7, 0.0);
		expBB.add(21);
		expBB.add(22);
		expBB.add(23);

		//if(i == 4)
		expBB.add(24);
		expBr.add(8);
		expBr.add(9);
		distance.put(8, 0.0);
		distance.put(9, 0.0);
		expBB.add(25);
		expBB.add(26);
		expBB.add(27);


		//if(i != 5)
		expBB.add(28);
		expBr.add(10);
		expBr.add(11);
		distance.put(10, 0.0);
		distance.put(11, 0.0);
		expBB.add(29);
		expBB.add(30);
		expBB.add(31);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);
		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitchn1() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, -1),
				new Invoke(null, c0, lSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(104); // start method "tSwitch"
		expBB.add(112);// end method "tSwitch"

		expBr.add(45);
		expBB.add(111);

		distance.put(39, 1.0); // case: 0
		distance.put(40, 11.0); // case: 10
		distance.put(41, 21.0); // case: 20
		distance.put(42, 31.0); // case: 30
		distance.put(43, 41.0); // case: 40
		distance.put(44, 51.0); // case: 50
		distance.put(45, 0.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch0() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 0),
				new Invoke(null, c0, lSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(104); // start method "tSwitch"
		expBB.add(112);// end method "tSwitch"

		expBr.add(39);
		expBB.add(105);
		expBB.add(106);
		expBB.add(107);
		expBB.add(108);

		distance.put(39,  0.0); // case: 0
		distance.put(40, 10.0); // case: 10
		distance.put(41, 20.0); // case: 20
		distance.put(42, 30.0); // case: 30
		distance.put(43, 40.0); // case: 40
		distance.put(44, 50.0); // case: 50
		distance.put(45,  1.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch10() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 10),
				new Invoke(null, c0, lSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(104); // start method "tSwitch"
		expBB.add(112);// end method "tSwitch"

		expBr.add(40);
		expBB.add(106);
		expBB.add(107);
		expBB.add(108);

		distance.put(39, 10.0); // case: 0
		distance.put(40,  0.0); // case: 10
		distance.put(41, 10.0); // case: 20
		distance.put(42, 20.0); // case: 30
		distance.put(43, 30.0); // case: 40
		distance.put(44, 40.0); // case: 50
		distance.put(45,  1.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch20() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 20),
				new Invoke(null, c0, lSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(104); // start method "tSwitch"
		expBB.add(112);// end method "tSwitch"

		expBr.add(41);
		expBB.add(107);
		expBB.add(108);

		distance.put(39, 20.0); // case: 0
		distance.put(40, 10.0); // case: 10
		distance.put(41,  0.0); // case: 20
		distance.put(42, 10.0); // case: 30
		distance.put(43, 20.0); // case: 40
		distance.put(44, 30.0); // case: 50
		distance.put(45,  1.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch30() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 30),
				new Invoke(null, c0, lSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(104); // start method "tSwitch"
		expBB.add(112);// end method "tSwitch"

		expBr.add(42);
		expBB.add(108);

		distance.put(39, 30.0); // case: 0
		distance.put(40, 20.0); // case: 10
		distance.put(41, 10.0); // case: 20
		distance.put(42,  0.0); // case: 30
		distance.put(43, 10.0); // case: 40
		distance.put(44, 20.0); // case: 50
		distance.put(45,  1.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch40() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 40),
				new Invoke(null, c0, lSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(104); // start method "tSwitch"
		expBB.add(112);// end method "tSwitch"

		expBr.add(43);
		expBB.add(109);

		distance.put(39, 40.0); // case: 0
		distance.put(40, 30.0); // case: 10
		distance.put(41, 20.0); // case: 20
		distance.put(42, 10.0); // case: 30
		distance.put(43,  0.0); // case: 40
		distance.put(44, 10.0); // case: 50
		distance.put(45,  1.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch50() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 50),
				new Invoke(null, c0, lSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(104); // start method "tSwitch"
		expBB.add(112);// end method "tSwitch"

		expBr.add(44);
		expBB.add(110);

		distance.put(39, 50.0); // case: 0
		distance.put(40, 40.0); // case: 10
		distance.put(41, 30.0); // case: 20
		distance.put(42, 20.0); // case: 30
		distance.put(43, 10.0); // case: 40
		distance.put(44,  0.0); // case: 50
		distance.put(45,  1.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

	public void testTracklSwitch51() throws Exception {
		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new AssignPrimitive(i0, 51),
				new Invoke(null, c0, lSwitch, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		Map<Integer, Double> distance = new HashMap<Integer, Double>();
		Set<Integer> expBB = new HashSet<Integer>();
		Set<Integer> expBr = new HashSet<Integer>();

		expBB.add(4);  // constructor
		expBB.add(104); // start method "tSwitch"
		expBB.add(112);// end method "tSwitch"

		expBr.add(45);
		expBB.add(111);

		distance.put(39, 51.0); // case: 0
		distance.put(40, 41.0); // case: 10
		distance.put(41, 31.0); // case: 20
		distance.put(42, 21.0); // case: 30
		distance.put(43, 11.0); // case: 40
		distance.put(44,  1.0); // case: 50
		distance.put(45, 0.0); // default

		checkBBCov(cov, expBB);
		checkCondCov(cov, expBr);
		checkDistance(t, distance);
	}

}
