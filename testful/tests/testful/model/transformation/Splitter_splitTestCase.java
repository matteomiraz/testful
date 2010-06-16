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
import testful.testCut.DummyStateMachineCUT;

/**
 * @author matteo
 *
 */
public class Splitter_splitTestCase extends GenericTestCase {

	private List<Test> perform(Test test) throws Exception {

		test = SimplifierDynamic.singleton.perform(getFinder(), test);
		return Splitter.split(false, test);
	}

	public void testHardStatemachine1() throws Exception {
		DummyStateMachineCUT cut = new DummyStateMachineCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),

				new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),
				new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),

				new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[1] }),

				new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new AssignPrimitive(cut.chars[0], 'w'),
				new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),

				new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[1] })
		});


		Operation[][] expected = {
				{
					new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),

					new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),
					new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),

					new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),

					new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),
					new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),

					new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),

					new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),
					new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),

					new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),

					new Invoke(null, cut.cuts[0], cut.getState, new Reference[] { }),
					new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),

					new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[1] }),
				}
		};

		check(t, perform(t), expected, true);
	}

	public void testHardStatemachine2() throws Exception {
		DummyStateMachineCUT cut = new DummyStateMachineCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new AssignPrimitive(cut.chars[1], 'w'),
				new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),
		});

		Operation[][] expected = {
				{
					new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
					new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),
					new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),
				}
		};

		check(t, perform(t), expected, true);
	}

	public void testHardStatemachine3() throws Exception {
		DummyStateMachineCUT cut = new DummyStateMachineCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[1] }),

				new AssignPrimitive(cut.chars[0], '['),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),

				new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),
		});

		Operation[][] expected = {
				{
					new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
					new Invoke(null, cut.cuts[0], cut.nextChar, new Reference[] { }),
				}, {
					new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
					new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
					new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[1] }),
				}, {
					new AssignPrimitive(cut.chars[0], '['),
					new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
					new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),
					new Invoke(null, cut.cuts[0], cut.nextChar, new Reference[] { }),
				}
		};

		check(t, perform(t), expected, true);
	}

	public void testHardStatemachine4() throws Exception {
		DummyStateMachineCUT cut = new DummyStateMachineCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[1] }),

				new AssignPrimitive(cut.chars[0], '['),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),
				new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
				new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),

				new AssignPrimitive(cut.chars[1], (char) 3006),
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(cut.chars[0], cut.cuts[0], cut.nextChar, new Reference[] { }),
		});

		Operation[][] expected = {
				{
					new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
					new Invoke(cut.chars[1], cut.cuts[0], cut.nextChar, new Reference[] { }),
					new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[1] }),
				},
				{
					new AssignPrimitive(cut.chars[0], '['),
					new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
					new Invoke(null, cut.cuts[0], cut.nextChar_char, new Reference[] { cut.chars[0] }),
					new Invoke(null, cut.cuts[0], cut.nextChar, new Reference[] { }),
				}, {
					new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
					new Invoke(null, cut.cuts[0], cut.nextChar, new Reference[] { }),
				}
		};

		check(t, perform(t), expected, true);
	}


	public void testHardStatemachine5() throws Exception {
		DummyStateMachineCUT cut = new DummyStateMachineCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				/*  0 */	new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
				/*  1 */	new Invoke(cut.chars[2], cut.cuts[3], cut.nextChar, new Reference[] { }),
				/*  2 */	new Invoke(cut.chars[1], cut.cuts[3], cut.nextChar, new Reference[] { }),
				/*  3 */	new Invoke(cut.chars[0], cut.cuts[3], cut.nextChar, new Reference[] { }),
				/*  4 */	new CreateObject(cut.cuts[2], cut.cns, new Reference[] { }),
				/*  5 */	new Invoke(null, cut.cuts[2], cut.nextChar_char, new Reference[] { cut.chars[2] }),
				/*  6 */	new Invoke(cut.chars[2], cut.cuts[2], cut.nextChar, new Reference[] { }),
				/*  7 */	new Invoke(cut.chars[0], cut.cuts[2], cut.nextChar, new Reference[] { }),
				/*  8 */	new Invoke(null, cut.cuts[3], cut.nextChar_char, new Reference[] { cut.chars[1] }),
		});

		Operation[][] expected = {
				{
					/*  0 */	new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
					/*  1 */	new Invoke(cut.chars[2], cut.cuts[3], cut.nextChar, new Reference[] { }),
					/*  4 */	new CreateObject(cut.cuts[2], cut.cns, new Reference[] { }),
					/*  5 */	new Invoke(null, cut.cuts[2], cut.nextChar_char, new Reference[] { cut.chars[2] }),
					/*  6 */	new Invoke(null, cut.cuts[2], cut.nextChar, new Reference[] { }),
				}, {
					/*  0 */	new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
					/*  2 */	new Invoke(cut.chars[1], cut.cuts[3], cut.nextChar, new Reference[] { }),
					/*  8 */	new Invoke(null, cut.cuts[3], cut.nextChar_char, new Reference[] { cut.chars[1] }),
				}
		};

		check(t, perform(t), expected, true);
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

		check(t, perform(t), expected, true);
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
	 * apache_Fraction_3 = apache.Fraction.TWO_THIRDS;
	 * java_lang_Integer_0 = (int)-1;
	 * apache_Fraction_2 = apache.Fraction.THREE_QUARTERS;
	 * java_lang_Integer_2 = (int) apache_Fraction_2.hashCode();
	 * apache_Fraction_3.equals(java_lang_Integer_2);
	 * java_lang_Integer_1 = (int) apache_Fraction_3.hashCode();
	 * apache_Fraction_0 = apache.Fraction.MINUS_ONE;
	 * apache_Fraction_3 = new apache.Fraction(java_lang_Integer_1, java_lang_Integer_0);
	 * apache_Fraction_1 = apache_Fraction_3.subtract(apache_Fraction_0);
	 * apache_Fraction_3 = apache_Fraction_1.multiply(apache_Fraction_3);
	 * apache_Fraction_3 = apache_Fraction_3.multiply(apache_Fraction_2);
	 * java_lang_Integer_0 = (int)-1737650931;
	 * apache_Fraction_0 = apache_Fraction_3.subtract(java_lang_Integer_0);
	 * apache_Fraction_1 = apache.Fraction.ONE_FIFTH;
	 * java_lang_Integer_0 = (int)-2147483648;
	 * java_lang_Object_3 = apache_Fraction_1.subtract(java_lang_Integer_0);
	 */
	public void testFraction2() throws Exception {
		ApacheFractionCUT cut = new ApacheFractionCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignConstant(cut.cuts[3], cut.TWO_THIRDS),
				new AssignPrimitive(cut.ints[0], -1),
				new AssignConstant(cut.cuts[2], cut.THREE_QUARTERS),
				new Invoke(cut.ints[2], cut.cuts[2], cut.hashCode, new Reference[] { }),
				new Invoke(null, cut.cuts[3], cut.equals, new Reference[] { cut.ints[2] }),
				new Invoke(cut.ints[1], cut.cuts[3], cut.hashCode, new Reference[] { }),
				new AssignConstant(cut.cuts[0], cut.MINUS_ONE),
				new CreateObject(cut.cuts[3], cut.cns_int_int, new Reference[] { cut.ints[1], cut.ints[0] }),
				new Invoke(cut.cuts[1], cut.cuts[3], cut.subtract_Fraction, new Reference[] { cut.cuts[0] }),
				new Invoke(cut.cuts[3], cut.cuts[1], cut.multiply_Fraction, new Reference[] { cut.cuts[3] }),
				new Invoke(cut.cuts[3], cut.cuts[3], cut.multiply_Fraction, new Reference[] { cut.cuts[2] }),
				new AssignPrimitive(cut.ints[0], -1737650931),
				new Invoke(cut.cuts[0], cut.cuts[3], cut.subtract_int, new Reference[] { cut.ints[0] }),
				new AssignConstant(cut.cuts[1], cut.ONE_FIFTH),
				new AssignPrimitive(cut.ints[0], -2147483648),
				new Invoke(cut.objects[3], cut.cuts[1], cut.subtract_int, new Reference[] { cut.ints[0] })

		});

		Operation[][] expected = {
				{
					new AssignConstant(cut.cuts[3], cut.TWO_THIRDS),
					new AssignConstant(cut.cuts[2], cut.THREE_QUARTERS),
					new Invoke(cut.ints[2], cut.cuts[2], cut.hashCode, new Reference[] { }),
					new Invoke(null, cut.cuts[3], cut.equals, new Reference[] { cut.ints[2] }),
				}, {
					new AssignPrimitive(cut.ints[0], -1),
					new AssignConstant(cut.cuts[2], cut.THREE_QUARTERS),
					new Invoke(cut.ints[2], cut.cuts[2], cut.hashCode, new Reference[] { }), // cut.ints[1] alias of cut.ints[2]
					new AssignConstant(cut.cuts[0], cut.MINUS_ONE),
					new CreateObject(cut.cuts[3], cut.cns_int_int, new Reference[] { cut.ints[2], cut.ints[0] }), // rewrite cut.ints[1] with cut.ints[2] (aliases)
					new Invoke(cut.cuts[1], cut.cuts[3], cut.subtract_Fraction, new Reference[] { cut.cuts[0] }),
					new Invoke(cut.cuts[3], cut.cuts[1], cut.multiply_Fraction, new Reference[] { cut.cuts[3] }),
					new Invoke(cut.cuts[3], cut.cuts[3], cut.multiply_Fraction, new Reference[] { cut.cuts[2] }),
					new AssignPrimitive(cut.ints[0], -1737650931),
					new Invoke(cut.cuts[0], cut.cuts[3], cut.subtract_int, new Reference[] { cut.ints[0] }),
				}, {
					new AssignConstant(cut.cuts[1], cut.ONE_FIFTH),
					new AssignPrimitive(cut.ints[0], -2147483648),
					new Invoke(null, cut.cuts[1], cut.subtract_int, new Reference[] { cut.ints[0] })
				}
		};

		check(t, perform(t), expected, false);
	}
}
