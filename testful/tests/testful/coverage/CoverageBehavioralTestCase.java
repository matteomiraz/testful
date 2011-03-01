/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011  Matteo Miraz
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
import testful.coverage.behavior.AbstractorMethod;
import testful.coverage.behavior.AbstractorObjectState;
import testful.coverage.behavior.AbstractorRegistry;
import testful.coverage.behavior.BehaviorCoverage;
import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.testCut.TestCoverageBehaviorCUT;
import testful.utils.ElementManager;

/**
 * Test for the behavioral coverage tracking functionality
 * @author matteo
 */
public class CoverageBehavioralTestCase extends GenericTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BehaviorCoverage.DOT = false;
		BehaviorCoverage.resetLabels();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		BehaviorCoverage.resetLabels();
	}

	public void testXmlDescription() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		AbstractorRegistry ar = cut.abstractorRegistry;

		AbstractorObjectState c = ar.getAbstractorClass("test.coverage.Behavior");
		assertNotNull(c);

		AbstractorMethod m = ar.getAbstractorMethod("test.coverage.Behavior.<init>()");
		assertNotNull(m);
	}


	public void testCns() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>() - -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 0}\n", beh.toString());
	}

	public void testCnsBoolTrue() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], true),
				new CreateObject(cut.cuts[0], cut.cns_bool, new Reference[] { cut.bools[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(Z) - {p0: true} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 0}\n", beh.toString());
	}

	public void testCnsBoolFalse() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], false),
				new CreateObject(cut.cuts[0], cut.cns_bool, new Reference[] { cut.bools[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(Z) - {p0: false} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 0}\n", beh.toString());
	}

	public void testCnsIntN() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], -1),
				new CreateObject(cut.cuts[0], cut.cns_int, new Reference[] { cut.ints[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(I) - {p0 < 0} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {-Inf < this.getN() < 0}\n", beh.toString());
	}


	public void testCnsInt0() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 0),
				new CreateObject(cut.cuts[0], cut.cns_int, new Reference[] { cut.ints[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(I) - {p0 = 0} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 0}\n", beh.toString());
	}


	public void testCnsIntP1() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new CreateObject(cut.cuts[0], cut.cns_int, new Reference[] { cut.ints[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(I) - {p0 > 0} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 1}\n", beh.toString());
	}

	public void testCnsIntP2() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 2),
				new CreateObject(cut.cuts[0], cut.cns_int, new Reference[] { cut.ints[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(I) - {p0 > 0} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {1 < this.getN() < this.getThree()}\n", beh.toString());
	}

	public void testCnsIntP3() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 3),
				new CreateObject(cut.cuts[0], cut.cns_int, new Reference[] { cut.ints[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(I) - {p0 > 0} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = this.getThree()}\n", beh.toString());
	}

	public void testCnsIntP5() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 5),
				new CreateObject(cut.cuts[0], cut.cns_int, new Reference[] { cut.ints[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(I) - {p0 > 0} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getThree() < this.getN() < +Inf}\n", beh.toString());
	}

	public void testMerge() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], -1),
				new CreateObject(cut.cuts[0], cut.cns_int, new Reference[] { cut.ints[0] }),
				new AssignPrimitive(cut.ints[0], 5),
				new CreateObject(cut.cuts[0], cut.cns_int, new Reference[] { cut.ints[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(2.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(I) - {p0 > 0} -> S1\n" +
				"  <init>(I) - {p0 < 0} -> S2\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getThree() < this.getN() < +Inf}\n" +
				"S2: test.coverage.Behavior: {-Inf < this.getN() < 0}\n", beh.toString());
	}

	public void testCnsObjNull() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignConstant(cut.objects[0], null),
				new CreateObject(cut.cuts[0], cut.cns_object, new Reference[] { cut.objects[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(Ljava.lang.Object;) - {p0 is null} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 0}\n", beh.toString());
	}

	public void testCnsObjNotNull() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.obj_cns, new Reference[] { }),
				new CreateObject(cut.cuts[0], cut.cns_object, new Reference[] { cut.objects[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(Ljava.lang.Object;) - {p0 is not null} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 0}\n", beh.toString());
	}

	public void testCnsStringNull() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.strings[0], null),
				new CreateObject(cut.cuts[0], cut.cns_string, new Reference[] { cut.strings[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(Ljava.lang.String;) - {p0 is null} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 0}\n", beh.toString());
	}

	public void testCnsStringEmpty() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.strings[0], ""),
				new CreateObject(cut.cuts[0], cut.cns_string, new Reference[] { cut.strings[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(Ljava.lang.String;) - {p0: empty string} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 0}\n", beh.toString());
	}

	public void testCnsString() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.strings[0], "Hello world!"),
				new CreateObject(cut.cuts[0], cut.cns_string, new Reference[] { cut.strings[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(Ljava.lang.String;) - {p0: non-empty string} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {this.getN() = 0}\n", beh.toString());
	}

	public void testCnsBehaviorNull() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignConstant(cut.cuts[0], null),
				new CreateObject(cut.cuts[1], cut.cns_beh, new Reference[] { cut.cuts[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(0.0f, beh.getQuality());
		assertEquals("S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testCnsBehaviorNotNull() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], -1),
				new CreateObject(cut.cuts[0], cut.cns_int, new Reference[] { cut.ints[0] }),
				new CreateObject(cut.cuts[1], cut.cns_beh, new Reference[] { cut.cuts[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(2.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>(I) - {p0 < 0} -> S1\n" +
				"  <init>(Ltest.coverage.Behavior;) - {p0: {-Inf < this.getN() < 0}} -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior: {-Inf < this.getN() < 0}\n", beh.toString());
	}

	public void testStaticMethodBoolFalse() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], false),
				new AssignPrimitive(cut.bools[1], true),
				new Invoke(null, null, cut.sMethod0, new Reference[] { cut.bools[0], cut.bools[1] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod0(ZZ) - {p0 && p1: false} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testStaticMethodBoolTrue() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.bools[0], true),
				new AssignPrimitive(cut.bools[1], true),
				new Invoke(null, null, cut.sMethod0, new Reference[] { cut.bools[0], cut.bools[1] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod0(ZZ) - {p0 && p1: true} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testStaticMethodIntA() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 0),
				new AssignPrimitive(cut.ints[1], 1),
				new Invoke(null, null, cut.sMethod1, new Reference[] { cut.ints[0], cut.ints[1] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod1(II) - {-Inf < p0 < p1} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testStaticMethodIntB() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 0),
				new AssignPrimitive(cut.ints[1], 0),
				new Invoke(null, null, cut.sMethod1, new Reference[] { cut.ints[0], cut.ints[1] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod1(II) - {p0 = p1} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testStaticMethodIntC() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 1),
				new AssignPrimitive(cut.ints[1], 0),
				new Invoke(null, null, cut.sMethod1, new Reference[] { cut.ints[0], cut.ints[1] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod1(II) - {p1 < p0 < +Inf} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testStaticMethodObjectA() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.obj_cns, new Reference[] { }),
				new CreateObject(cut.objects[1], cut.obj_cns, new Reference[] { }),
				new Invoke(null, null, cut.sMethod2, new Reference[] { cut.objects[0], cut.objects[1] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod2(Ljava.lang.Object;Ljava.lang.Object;) - {p0:[]} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testStaticMethodObjectB() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.obj_cns, new Reference[] { }),
				new Invoke(null, null, cut.sMethod2, new Reference[] { cut.objects[0], cut.objects[0] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod2(Ljava.lang.Object;Ljava.lang.Object;) - {p0:[p1]} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testStaticMethodObjectC() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignConstant(cut.objects[0], null),
				new CreateObject(cut.objects[1], cut.obj_cns, new Reference[] { }),
				new Invoke(null, null, cut.sMethod2, new Reference[] { cut.objects[0], cut.objects[1] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod2(Ljava.lang.Object;Ljava.lang.Object;) - {p0:[null]} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testStaticMethodObjectD() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.obj_cns, new Reference[] { }),
				new AssignConstant(cut.objects[1], null),
				new Invoke(null, null, cut.sMethod2, new Reference[] { cut.objects[0], cut.objects[1] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod2(Ljava.lang.Object;Ljava.lang.Object;) - {p0:[]} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}

	public void testStaticMethodObjectE() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignConstant(cut.objects[0], null),
				new AssignConstant(cut.objects[1], null),
				new Invoke(null, null, cut.sMethod2, new Reference[] { cut.objects[0], cut.objects[1] }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  sMethod2(Ljava.lang.Object;Ljava.lang.Object;) - {p0:[null, p1]} -> S0\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n", beh.toString());
	}
}