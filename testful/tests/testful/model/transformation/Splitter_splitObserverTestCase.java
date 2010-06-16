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

package testful.model.transformation;

import java.util.List;

import testful.GenericTestCase;
import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.testCut.ApacheFractionCUT;
import testful.testCut.DummySimpleCUT;
import testful.testCut.TestCoverageFaultCUT;

/**
 * @author matteo
 *
 */
public class Splitter_splitObserverTestCase extends GenericTestCase {

	private List<Test> perform(Test test) throws Exception {

		test = SimplifierDynamic.singleton.perform(getFinder(), test);

		return Splitter.split(true, test);
	}

	public void testFault() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[3], cut.oCns, new Reference[] { }),		//1
				new CreateObject(cut.cuts[1], cut.cCns, new Reference[] { }),			//2
				new Invoke(null, cut.cuts[1], cut.c, new Reference[] { }),				//3 -> faulty
				new CreateObject(cut.cuts[1], cut.cCns, new Reference[] { }),			//4
				new Invoke(null, cut.cuts[1], cut.a, new Reference[] { cut.objects[3]})	//5
		});

		List<Test> tests = perform(test);

		check(test, tests, new Operation[][] {
				{
					new CreateObject(null, cut.oCns, new Reference[] { }),					//1*
				}, {
					new CreateObject(cut.cuts[1], cut.cCns, new Reference[] { }),			//2
					new Invoke(null, cut.cuts[1], cut.c, new Reference[] { }),				//3
				}, {
					new CreateObject(cut.cuts[1], cut.cCns, new Reference[] { }),			//4
					new Invoke(null, cut.cuts[1], cut.a, new Reference[] { cut.objects[3]})	//5
				}
		}, true);
	}

	public void testSimple() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[0], cut.oCns, new Reference[] { }),		//1
				new CreateObject(cut.objects[3], cut.cns, new Reference[] { }),			//2
		});

		List<Test> tests = perform(test);

		check(test, tests, new Operation[][] {
				{
					new CreateObject(null, cut.cns, new Reference[] { }),				//2*
				}
		}, true);
	}

	/**
	 * java_lang_Integer_1 = (int)2147483647;
	 * java_lang_Double_3 = (double)-0.10100931144814085;
	 * java_lang_Object_3 = new apache.Fraction(java_lang_Double_3, java_lang_Integer_1);
	 */
	public void testFraction1() throws Exception {
		ApacheFractionCUT cut = new ApacheFractionCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[1], 2147483647),
				new AssignPrimitive(cut.doubles[3], -0.10100931144814085),
				new CreateObject(cut.objects[3], cut.cns_double_int, new Reference[] { cut.doubles[3], cut.ints[1] })
		});

		Operation[][] expected = {
				{
					new AssignPrimitive(cut.ints[1], 2147483647),
					new AssignPrimitive(cut.doubles[3], -0.10100931144814085),
					new CreateObject(null, cut.cns_double_int, new Reference[] { cut.doubles[3], cut.ints[1] })
				}
		};

		// skip coverage checks (to avoid problems with the static initializer)
		check(t, perform(t), expected, false);
	}

	/**
	 * java_lang_Integer_2 = (int)-75350235;
	 * dummy_Simple_1 = new dummy.Simple();
	 * java_lang_Integer_3 = (int) dummy_Simple_1.wModulo(java_lang_Integer_2);
	 * java_lang_Integer_0 = (int)-2147483648;
	 * dummy_Simple_3 = new dummy.Simple();
	 * dummy_Simple_3.compare(java_lang_Object_1);
	 * dummy_Simple_3.compare(java_lang_Integer_0);
	 * dummy_Simple_2 = new dummy.Simple();
	 * dummy_Simple_3 = null;
	 * dummy_Simple_2.compare(dummy_Simple_3);
	 * java_lang_Integer_2 = (int) dummy_Simple_2.wModulo(java_lang_Integer_0);
	 */
	public void testSimple1() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[2], -75350235),
				new CreateObject(cut.cuts[1], cut.cns, new Reference[] { }),
				new Invoke(cut.ints[3], cut.cuts[1], cut.wModulo, new Reference[] { cut.ints[2] }),
				new AssignPrimitive(cut.ints[0], -2147483648),
				new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[3], cut.compare, new Reference[] { cut.objects[1] }),
				new Invoke(null, cut.cuts[3], cut.compare, new Reference[] { cut.ints[0] }),
				new CreateObject(cut.cuts[2], cut.cns, new Reference[] { }),
				new AssignConstant(cut.cuts[3], null),
				new Invoke(null, cut.cuts[2], cut.compare, new Reference[] { cut.cuts[3] }),
				new Invoke(cut.ints[2], cut.cuts[2], cut.wModulo, new Reference[] { cut.ints[0] })
		});

		Operation[][] expected = {
				{
					new AssignPrimitive(cut.ints[2], -75350235),
					new CreateObject(cut.cuts[1], cut.cns, new Reference[] { }),
					new Invoke(null, cut.cuts[1], cut.wModulo, new Reference[] { cut.ints[2] }),
				}, {
					new AssignPrimitive(cut.ints[0], -2147483648),
					new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
					new Invoke(null, cut.cuts[3], cut.compare, new Reference[] { cut.objects[1] }),
					new Invoke(null, cut.cuts[3], cut.compare, new Reference[] { cut.ints[0] }),
					new CreateObject(cut.cuts[2], cut.cns, new Reference[] { }),
					new AssignConstant(cut.cuts[3], null),
					new Invoke(null, cut.cuts[2], cut.compare, new Reference[] { cut.cuts[3] }),
					new Invoke(null, cut.cuts[2], cut.wModulo, new Reference[] { cut.ints[0] })
				}
		};

		check(t, perform(t), expected, true);
	}

	/**
	 * java_lang_Integer_3 = (int)9;
	 * java_lang_Object_3 = new apache.Fraction(java_lang_Integer_3);
	 * java_lang_Object_3 = new apache.Fraction(java_lang_Integer_3, java_lang_Integer_3);
	 */
	public void testFraction2() throws Exception {
		ApacheFractionCUT cut = new ApacheFractionCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[3], 9),
				new CreateObject(cut.objects[3], cut.cns_int, new Reference[] { cut.ints[3] } ),
				new CreateObject(cut.objects[3], cut.cns_int_int, new Reference[] { cut.ints[3], cut.ints[3] } ),
		});

		Operation[][] expected = {
				{
					new AssignPrimitive(cut.ints[3], 9),
					new CreateObject(null, cut.cns_int, new Reference[] { cut.ints[3] } ),
				}, {
					new AssignPrimitive(cut.ints[3], 9),
					new CreateObject(null, cut.cns_int_int, new Reference[] { cut.ints[3], cut.ints[3] } ),
				}
		};

		// skip coverage checks (to avoid problems with the static initializer)
		check(t, perform(t), expected, false);
	}
}
