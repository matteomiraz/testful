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

package testful.model;

import testful.GenericTestCase;
import testful.testCut.ApacheFractionCUT;

/**
 * Ensures that Testful has a repeatable behavior
 * @author matteo
 */
public class RepeatableTestCase extends GenericTestCase {

	public void testClusterApache() throws Exception {
		ApacheFractionCUT cut = new ApacheFractionCUT();
		Clazz[] cluster = cut.cluster.getCluster();

		assertEquals(4, cluster.length);

		assertEquals("apache.Fraction", cluster[0].getClassName());
		assertEquals("apache.Fraction", cluster[0].getReferenceClazz().getClassName());

		// cluster[0].getSubClasses()
		assertEquals(0, cluster[0].getSubClasses().length);

		// cluster[0].getAssignableTo()
		assertEquals(2, cluster[0].getAssignableTo().length);
		assertEquals("apache.Fraction", cluster[0].getAssignableTo()[0].toString());
		assertEquals("java.lang.Object", cluster[0].getAssignableTo()[1].toString());

		// cluster[0].getConstants()
		assertEquals(14, cluster[0].getConstants().length);
		assertEquals("apache.Fraction.FOUR_FIFTHS", cluster[0].getConstants()[0].toString());
		assertEquals("apache.Fraction.MINUS_ONE", cluster[0].getConstants()[1].toString());
		assertEquals("apache.Fraction.ONE", cluster[0].getConstants()[2].toString());
		assertEquals("apache.Fraction.ONE_FIFTH", cluster[0].getConstants()[3].toString());
		assertEquals("apache.Fraction.ONE_HALF", cluster[0].getConstants()[4].toString());
		assertEquals("apache.Fraction.ONE_QUARTER", cluster[0].getConstants()[5].toString());
		assertEquals("apache.Fraction.ONE_THIRD", cluster[0].getConstants()[6].toString());
		assertEquals("apache.Fraction.THREE_FIFTHS", cluster[0].getConstants()[7].toString());
		assertEquals("apache.Fraction.THREE_QUARTERS", cluster[0].getConstants()[8].toString());
		assertEquals("apache.Fraction.TWO", cluster[0].getConstants()[9].toString());
		assertEquals("apache.Fraction.TWO_FIFTHS", cluster[0].getConstants()[10].toString());
		assertEquals("apache.Fraction.TWO_QUARTERS", cluster[0].getConstants()[11].toString());
		assertEquals("apache.Fraction.TWO_THIRDS", cluster[0].getConstants()[12].toString());
		assertEquals("apache.Fraction.ZERO", cluster[0].getConstants()[13].toString());

		// cluster[0].getConstructors()
		assertEquals(5, cluster[0].getConstructors().length);
		assertEquals("apache.Fraction(double)", cluster[0].getConstructors()[0].toString());
		assertEquals("apache.Fraction(int)", cluster[0].getConstructors()[1].toString());
		assertEquals("apache.Fraction(double, int)", cluster[0].getConstructors()[2].toString());
		assertEquals("apache.Fraction(int, int)", cluster[0].getConstructors()[3].toString());
		assertEquals("apache.Fraction(double, double, int)", cluster[0].getConstructors()[4].toString());

		// cluster[0].getMethods()
		assertEquals(23, cluster[0].getMethods().length);
		assertEquals("abs()", cluster[0].getMethods()[0].toString());
		assertEquals("add(apache.Fraction)", cluster[0].getMethods()[1].toString());
		assertEquals("add(int)", cluster[0].getMethods()[2].toString());
		assertEquals("byteValue()", cluster[0].getMethods()[3].toString());
		assertEquals("compareTo(apache.Fraction)", cluster[0].getMethods()[4].toString());
		assertEquals("divide(apache.Fraction)", cluster[0].getMethods()[5].toString());
		assertEquals("divide(int)", cluster[0].getMethods()[6].toString());
		assertEquals("doubleValue()", cluster[0].getMethods()[7].toString());
		assertEquals("equals(java.lang.Object)", cluster[0].getMethods()[8].toString());
		assertEquals("floatValue()", cluster[0].getMethods()[9].toString());
		assertEquals("getDenominator()", cluster[0].getMethods()[10].toString());
		assertEquals("getNumerator()", cluster[0].getMethods()[11].toString());
		assertEquals("hashCode()", cluster[0].getMethods()[12].toString());
		assertEquals("intValue()", cluster[0].getMethods()[13].toString());
		assertEquals("longValue()", cluster[0].getMethods()[14].toString());
		assertEquals("multiply(apache.Fraction)", cluster[0].getMethods()[15].toString());
		assertEquals("multiply(int)", cluster[0].getMethods()[16].toString());
		assertEquals("negate()", cluster[0].getMethods()[17].toString());
		assertEquals("reciprocal()", cluster[0].getMethods()[18].toString());
		assertEquals("shortValue()", cluster[0].getMethods()[19].toString());
		assertEquals("subtract(apache.Fraction)", cluster[0].getMethods()[20].toString());
		assertEquals("subtract(int)", cluster[0].getMethods()[21].toString());
		assertEquals("toString()", cluster[0].getMethods()[22].toString());


		assertEquals("java.lang.Double", cluster[1].getClassName());
		assertEquals("java.lang.Double", cluster[1].getReferenceClazz().getClassName());

		// cluster[1].getSubClasses()
		assertEquals(0, cluster[1].getSubClasses().length);

		// cluster[1].getAssignableTo()
		assertEquals(3, cluster[1].getAssignableTo().length);
		assertEquals("java.lang.Double", cluster[1].getAssignableTo()[0].toString());
		assertEquals("java.lang.Integer", cluster[1].getAssignableTo()[1].toString());
		assertEquals("java.lang.Object", cluster[1].getAssignableTo()[2].toString());

		// cluster[1].getConstants()
		assertEquals(9, cluster[1].getConstants().length);
		assertEquals("java.lang.Double.MAX_EXPONENT", cluster[1].getConstants()[0].toString());
		assertEquals("java.lang.Double.MAX_VALUE", cluster[1].getConstants()[1].toString());
		assertEquals("java.lang.Double.MIN_EXPONENT", cluster[1].getConstants()[2].toString());
		assertEquals("java.lang.Double.MIN_NORMAL", cluster[1].getConstants()[3].toString());
		assertEquals("java.lang.Double.MIN_VALUE", cluster[1].getConstants()[4].toString());
		assertEquals("java.lang.Double.NEGATIVE_INFINITY", cluster[1].getConstants()[5].toString());
		assertEquals("java.lang.Double.NaN", cluster[1].getConstants()[6].toString());
		assertEquals("java.lang.Double.POSITIVE_INFINITY", cluster[1].getConstants()[7].toString());
		assertEquals("java.lang.Double.SIZE", cluster[1].getConstants()[8].toString());

		// cluster[1].getConstructors()
		assertEquals(0, cluster[1].getConstructors().length);

		// cluster[1].getMethods()
		assertEquals(0, cluster[1].getMethods().length);


		assertEquals("double", ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getClassName());
		assertEquals("java.lang.Double", ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getReferenceClazz().getClassName());

		// ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getSubClasses()
		assertEquals(0, ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getSubClasses().length);

		// ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getAssignableTo()
		assertEquals(2, ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getAssignableTo().length);
		assertEquals("java.lang.Double", ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getAssignableTo()[0].toString());
		assertEquals("java.lang.Integer", ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getAssignableTo()[1].toString());

		// ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getConstants()
		assertEquals(0, ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getConstants().length);

		// ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getConstructors()
		assertEquals(0, ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getConstructors().length);

		// ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getMethods()
		assertEquals(0, ((PrimitiveClazz)cluster[1]).getPrimitiveClazz().getMethods().length);


		assertEquals("java.lang.Integer", cluster[2].getClassName());
		assertEquals("java.lang.Integer", cluster[2].getReferenceClazz().getClassName());

		// cluster[2].getSubClasses()
		assertEquals(0, cluster[2].getSubClasses().length);

		// cluster[2].getAssignableTo()
		assertEquals(3, cluster[2].getAssignableTo().length);
		assertEquals("java.lang.Double", cluster[2].getAssignableTo()[0].toString());
		assertEquals("java.lang.Integer", cluster[2].getAssignableTo()[1].toString());
		assertEquals("java.lang.Object", cluster[2].getAssignableTo()[2].toString());

		// cluster[2].getConstants()
		assertEquals(9, cluster[2].getConstants().length);
		assertEquals("java.lang.Double.MAX_EXPONENT", cluster[2].getConstants()[0].toString());
		assertEquals("java.lang.Double.MAX_VALUE", cluster[2].getConstants()[1].toString());
		assertEquals("java.lang.Double.MIN_EXPONENT", cluster[2].getConstants()[2].toString());
		assertEquals("java.lang.Double.MIN_NORMAL", cluster[2].getConstants()[3].toString());
		assertEquals("java.lang.Double.MIN_VALUE", cluster[2].getConstants()[4].toString());
		assertEquals("java.lang.Double.NEGATIVE_INFINITY", cluster[2].getConstants()[5].toString());
		assertEquals("java.lang.Double.NaN", cluster[2].getConstants()[6].toString());
		assertEquals("java.lang.Double.POSITIVE_INFINITY", cluster[2].getConstants()[7].toString());
		assertEquals("java.lang.Double.SIZE", cluster[2].getConstants()[8].toString());

		// cluster[2].getConstructors()
		assertEquals(0, cluster[2].getConstructors().length);

		// cluster[2].getMethods()
		assertEquals(0, cluster[2].getMethods().length);


		assertEquals("int", ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getClassName());
		assertEquals("java.lang.Integer", ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getReferenceClazz().getClassName());

		// ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getSubClasses()
		assertEquals(0, ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getSubClasses().length);

		// ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getAssignableTo()
		assertEquals(2, ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getAssignableTo().length);
		assertEquals("java.lang.Double", ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getAssignableTo()[0].toString());
		assertEquals("java.lang.Integer", ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getAssignableTo()[1].toString());

		// ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getConstants()
		assertEquals(0, ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getConstants().length);

		// ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getConstructors()
		assertEquals(0, ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getConstructors().length);

		// ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getMethods()
		assertEquals(0, ((PrimitiveClazz)cluster[2]).getPrimitiveClazz().getMethods().length);


		assertEquals("java.lang.Object", cluster[3].getClassName());
		assertEquals("java.lang.Object", cluster[3].getReferenceClazz().getClassName());

		// cluster[3].getSubClasses()
		assertEquals(3, cluster[3].getSubClasses().length);
		assertEquals("apache.Fraction", cluster[3].getSubClasses()[0].toString());
		assertEquals("java.lang.Double", cluster[3].getSubClasses()[1].toString());
		assertEquals("java.lang.Integer", cluster[3].getSubClasses()[2].toString());

		// cluster[3].getAssignableTo()
		assertEquals(1, cluster[3].getAssignableTo().length);
		assertEquals("java.lang.Object", cluster[3].getAssignableTo()[0].toString());

		// cluster[3].getConstants()
		assertEquals(14, cluster[3].getConstants().length);
		assertEquals("apache.Fraction.FOUR_FIFTHS", cluster[3].getConstants()[0].toString());
		assertEquals("apache.Fraction.MINUS_ONE", cluster[3].getConstants()[1].toString());
		assertEquals("apache.Fraction.ONE", cluster[3].getConstants()[2].toString());
		assertEquals("apache.Fraction.ONE_FIFTH", cluster[3].getConstants()[3].toString());
		assertEquals("apache.Fraction.ONE_HALF", cluster[3].getConstants()[4].toString());
		assertEquals("apache.Fraction.ONE_QUARTER", cluster[3].getConstants()[5].toString());
		assertEquals("apache.Fraction.ONE_THIRD", cluster[3].getConstants()[6].toString());
		assertEquals("apache.Fraction.THREE_FIFTHS", cluster[3].getConstants()[7].toString());
		assertEquals("apache.Fraction.THREE_QUARTERS", cluster[3].getConstants()[8].toString());
		assertEquals("apache.Fraction.TWO", cluster[3].getConstants()[9].toString());
		assertEquals("apache.Fraction.TWO_FIFTHS", cluster[3].getConstants()[10].toString());
		assertEquals("apache.Fraction.TWO_QUARTERS", cluster[3].getConstants()[11].toString());
		assertEquals("apache.Fraction.TWO_THIRDS", cluster[3].getConstants()[12].toString());
		assertEquals("apache.Fraction.ZERO", cluster[3].getConstants()[13].toString());

		// cluster[3].getConstructors()
		assertEquals(1, cluster[3].getConstructors().length);
		assertEquals("java.lang.Object()", cluster[3].getConstructors()[0].toString());

		// cluster[3].getMethods()
		assertEquals(0, cluster[3].getMethods().length);
	}

	public void testReferenceFactory() throws Exception {
		ApacheFractionCUT cut = new ApacheFractionCUT();
		Reference[] refs = cut.refFactory.getReferences();

		assertEquals(0, refs[0].getId());
		assertEquals(0, refs[0].getPos());
		assertEquals("apache.Fraction", refs[0].getClazz().getClassName());

		assertEquals(1, refs[1].getId());
		assertEquals(1, refs[1].getPos());
		assertEquals("apache.Fraction", refs[1].getClazz().getClassName());

		assertEquals(2, refs[2].getId());
		assertEquals(2, refs[2].getPos());
		assertEquals("apache.Fraction", refs[2].getClazz().getClassName());

		assertEquals(3, refs[3].getId());
		assertEquals(3, refs[3].getPos());
		assertEquals("apache.Fraction", refs[3].getClazz().getClassName());

		assertEquals(4, refs[4].getId());
		assertEquals(0, refs[4].getPos());
		assertEquals("java.lang.Double", refs[4].getClazz().getClassName());

		assertEquals(5, refs[5].getId());
		assertEquals(1, refs[5].getPos());
		assertEquals("java.lang.Double", refs[5].getClazz().getClassName());

		assertEquals(6, refs[6].getId());
		assertEquals(2, refs[6].getPos());
		assertEquals("java.lang.Double", refs[6].getClazz().getClassName());

		assertEquals(7, refs[7].getId());
		assertEquals(3, refs[7].getPos());
		assertEquals("java.lang.Double", refs[7].getClazz().getClassName());

		assertEquals(8, refs[8].getId());
		assertEquals(0, refs[8].getPos());
		assertEquals("java.lang.Integer", refs[8].getClazz().getClassName());

		assertEquals(9, refs[9].getId());
		assertEquals(1, refs[9].getPos());
		assertEquals("java.lang.Integer", refs[9].getClazz().getClassName());

		assertEquals(10, refs[10].getId());
		assertEquals(2, refs[10].getPos());
		assertEquals("java.lang.Integer", refs[10].getClazz().getClassName());

		assertEquals(11, refs[11].getId());
		assertEquals(3, refs[11].getPos());
		assertEquals("java.lang.Integer", refs[11].getClazz().getClassName());

		assertEquals(12, refs[12].getId());
		assertEquals(0, refs[12].getPos());
		assertEquals("java.lang.Object", refs[12].getClazz().getClassName());

		assertEquals(13, refs[13].getId());
		assertEquals(1, refs[13].getPos());
		assertEquals("java.lang.Object", refs[13].getClazz().getClassName());

		assertEquals(14, refs[14].getId());
		assertEquals(2, refs[14].getPos());
		assertEquals("java.lang.Object", refs[14].getClazz().getClassName());

		assertEquals(15, refs[15].getId());
		assertEquals(3, refs[15].getPos());
		assertEquals("java.lang.Object", refs[15].getClazz().getClassName());
	}

	public void testRandomGeneration() throws Exception {
		Operation[] ops = GenericTestCase.createRandomTest("apache.Fraction", 10, 12012010L).getTest();

		assertEquals(10, ops.length);
		assertEquals("java_lang_Double_2 = (double) apache_Fraction_2.intValue()", ops[0].toString());
		assertEquals("java_lang_Integer_0 = (int)0", ops[1].toString());
		assertEquals("apache_Fraction_3 = apache.Fraction.ZERO", ops[2].toString());
		assertEquals("apache_Fraction_0 = apache_Fraction_3.subtract(java_lang_Integer_0)", ops[3].toString());
		assertEquals("java_lang_Object_1 = new java.lang.Object()", ops[4].toString());
		assertEquals("java_lang_Object_2 = apache.Fraction.ONE", ops[5].toString());
		assertEquals("apache_Fraction_2 = apache.Fraction.FOUR_FIFTHS", ops[6].toString());
		assertEquals("apache_Fraction_3 = apache_Fraction_1.multiply(apache_Fraction_2)", ops[7].toString());
		assertEquals("apache_Fraction_0 = apache.Fraction.THREE_QUARTERS", ops[8].toString());
		assertEquals("apache_Fraction_1 = apache_Fraction_0.reciprocal()", ops[9].toString());
	}

	public void testRandomGenerationExtended() throws Exception {
		Operation[] ops1 = GenericTestCase.createRandomTest("apache.Fraction", 1000, 12012010l).getTest();
		Operation[] ops2 = GenericTestCase.createRandomTest("apache.Fraction", 1000, 12012010l).getTest();

		assertEquals(1000, ops1.length);
		assertEquals(1000, ops2.length);

		for (int i = 0; i < 1000; i++)
			assertEquals(ops1[i], ops2[i]);
	}

	public void testHashCodeFast() throws Exception {
		ApacheFractionCUT cut = new ApacheFractionCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.doubles[3], -0.4445457912686642),
				new AssignPrimitive(cut.ints[2], 5),
				new CreateObject(cut.cuts[0], cut.cns_double_int, new Reference[] { cut.doubles[3], cut.ints[2] }),
				new Invoke(null, cut.cuts[0], cut.divide_Fraction, new Reference[] { cut.cuts[0] } )
		});

		assertEquals(-1894196417, t.getTest()[0].hashCode());
		assertEquals(       8995, t.getTest()[1].hashCode());
		assertEquals(  361810880, t.getTest()[2].hashCode());
		assertEquals( -834852340, t.getTest()[3].hashCode());
		assertEquals( -924205935, t.hashCode());
	}

	public void testHashCodeLong() throws Exception {
		Test t = GenericTestCase.createRandomTest("apache.Fraction", 100, 12012010l);

		assertEquals(100, t.getTest().length);

		assertEquals("java_lang_Double_2 = (double) apache_Fraction_2.intValue()", t.getTest()[0].toString());
		assertEquals("java_lang_Integer_0 = (int)0", t.getTest()[1].toString());
		assertEquals("apache_Fraction_3 = apache.Fraction.ZERO", t.getTest()[2].toString());
		assertEquals("apache_Fraction_0 = apache_Fraction_3.subtract(java_lang_Integer_0)", t.getTest()[3].toString());
		assertEquals("java_lang_Object_1 = new java.lang.Object()", t.getTest()[4].toString());
		assertEquals("java_lang_Object_2 = apache.Fraction.ONE", t.getTest()[5].toString());
		assertEquals("apache_Fraction_2 = apache.Fraction.FOUR_FIFTHS", t.getTest()[6].toString());
		assertEquals("apache_Fraction_3 = apache_Fraction_1.multiply(apache_Fraction_2)", t.getTest()[7].toString());
		assertEquals("apache_Fraction_0 = apache.Fraction.THREE_QUARTERS", t.getTest()[8].toString());
		assertEquals("apache_Fraction_1 = apache_Fraction_0.reciprocal()", t.getTest()[9].toString());
		assertEquals("java_lang_Integer_3 = (int) apache_Fraction_2.intValue()", t.getTest()[10].toString());
		assertEquals("java_lang_Double_2 = (double)0.32649530144511274", t.getTest()[11].toString());
		assertEquals("java_lang_Integer_1 = (int) apache_Fraction_1.getNumerator()", t.getTest()[12].toString());
		assertEquals("java_lang_Integer_2 = (int) apache_Fraction_0.getDenominator()", t.getTest()[13].toString());
		assertEquals("java_lang_Double_0 = (double)0.26442080576221294", t.getTest()[14].toString());
		assertEquals("java_lang_Object_3 = apache_Fraction_0.toString()", t.getTest()[15].toString());
		assertEquals("apache_Fraction_3 = apache_Fraction_2.add(java_lang_Integer_1)", t.getTest()[16].toString());
		assertEquals("java_lang_Object_1 = apache.Fraction.ZERO", t.getTest()[17].toString());
		assertEquals("java_lang_Object_1 = apache.Fraction.THREE_FIFTHS", t.getTest()[18].toString());
		assertEquals("java_lang_Integer_2 = (int)-1", t.getTest()[19].toString());
		assertEquals("apache_Fraction_3 = apache_Fraction_3.multiply(java_lang_Integer_1)", t.getTest()[20].toString());
		assertEquals("java_lang_Object_0 = apache.Fraction.ONE", t.getTest()[21].toString());
		assertEquals("java_lang_Integer_1 = (int) apache_Fraction_2.intValue()", t.getTest()[22].toString());
		assertEquals("apache_Fraction_0 = new apache.Fraction(java_lang_Double_0, java_lang_Double_0, java_lang_Integer_3)", t.getTest()[23].toString());
		assertEquals("apache_Fraction_3 = apache_Fraction_1.add(apache_Fraction_0)", t.getTest()[24].toString());
		assertEquals("java_lang_Double_0 = (double) apache_Fraction_3.getDenominator()", t.getTest()[25].toString());
		assertEquals("java_lang_Double_3 = (double) apache_Fraction_3.intValue()", t.getTest()[26].toString());
		assertEquals("java_lang_Double_0 = (double)4.4110842776770527E307", t.getTest()[27].toString());
		assertEquals("apache_Fraction_0 = apache_Fraction_1.divide(java_lang_Integer_1)", t.getTest()[28].toString());
		assertEquals("java_lang_Integer_3 = (int) apache_Fraction_2.hashCode()", t.getTest()[29].toString());
		assertEquals("apache_Fraction_0 = apache.Fraction.ONE_HALF", t.getTest()[30].toString());
		assertEquals("java_lang_Object_2 = apache_Fraction_0.reciprocal()", t.getTest()[31].toString());
		assertEquals("java_lang_Double_2 = (double)0.17511881932984608", t.getTest()[32].toString());
		assertEquals("java_lang_Object_3 = apache.Fraction.TWO", t.getTest()[33].toString());
		assertEquals("java_lang_Double_2 = (double) apache_Fraction_1.getNumerator()", t.getTest()[34].toString());
		assertEquals("java_lang_Integer_2 = (int) apache_Fraction_0.floatValue()", t.getTest()[35].toString());
		assertEquals("java_lang_Object_3 = apache_Fraction_0.add(java_lang_Integer_2)", t.getTest()[36].toString());
		assertEquals("java_lang_Object_1 = apache.Fraction.ONE_QUARTER", t.getTest()[37].toString());
		assertEquals("java_lang_Double_1 = (double) apache_Fraction_0.floatValue()", t.getTest()[38].toString());
		assertEquals("java_lang_Object_2 = apache.Fraction.ZERO", t.getTest()[39].toString());
		assertEquals("java_lang_Double_0 = (double) apache_Fraction_1.doubleValue()", t.getTest()[40].toString());
		assertEquals("apache_Fraction_1 = apache.Fraction.TWO_THIRDS", t.getTest()[41].toString());
		assertEquals("java_lang_Integer_0 = (int)1", t.getTest()[42].toString());
		assertEquals("apache_Fraction_0 = new apache.Fraction(java_lang_Double_3)", t.getTest()[43].toString());
		assertEquals("java_lang_Object_3 = apache.Fraction.ONE_HALF", t.getTest()[44].toString());
		assertEquals("java_lang_Object_1 = apache.Fraction.ONE_THIRD", t.getTest()[45].toString());
		assertEquals("apache_Fraction_0 = apache_Fraction_0.multiply(apache_Fraction_3)", t.getTest()[46].toString());
		assertEquals("java_lang_Object_0 = apache.Fraction.ONE_THIRD", t.getTest()[47].toString());
		assertEquals("java_lang_Integer_0 = (int) apache_Fraction_0.byteValue()", t.getTest()[48].toString());
		assertEquals("java_lang_Integer_0 = (int) apache_Fraction_2.longValue()", t.getTest()[49].toString());
		assertEquals("java_lang_Object_3 = apache.Fraction.ONE_QUARTER", t.getTest()[50].toString());
		assertEquals("apache_Fraction_3.equals(java_lang_Double_3)", t.getTest()[51].toString());
		assertEquals("apache_Fraction_0 = apache.Fraction.ONE_HALF", t.getTest()[52].toString());
		assertEquals("java_lang_Object_1 = apache_Fraction_2.add(apache_Fraction_1)", t.getTest()[53].toString());
		assertEquals("apache_Fraction_2 = apache_Fraction_2.subtract(apache_Fraction_2)", t.getTest()[54].toString());
		assertEquals("apache_Fraction_2 = apache_Fraction_0.multiply(apache_Fraction_0)", t.getTest()[55].toString());
		assertEquals("apache_Fraction_3.equals(apache_Fraction_2)", t.getTest()[56].toString());
		assertEquals("apache_Fraction_3 = apache_Fraction_2.divide(apache_Fraction_3)", t.getTest()[57].toString());
		assertEquals("java_lang_Integer_1 = (int) apache_Fraction_1.hashCode()", t.getTest()[58].toString());
		assertEquals("java_lang_Object_2 = apache_Fraction_2.multiply(java_lang_Integer_2)", t.getTest()[59].toString());
		assertEquals("java_lang_Object_1 = new java.lang.Object()", t.getTest()[60].toString());
		assertEquals("apache_Fraction_0 = apache_Fraction_2.divide(apache_Fraction_1)", t.getTest()[61].toString());
		assertEquals("java_lang_Integer_1 = (int) apache_Fraction_1.getDenominator()", t.getTest()[62].toString());
		assertEquals("java_lang_Object_1 = new java.lang.Object()", t.getTest()[63].toString());
		assertEquals("java_lang_Object_1 = apache.Fraction.ONE", t.getTest()[64].toString());
		assertEquals("java_lang_Object_0 = new apache.Fraction(java_lang_Integer_1, java_lang_Integer_3)", t.getTest()[65].toString());
		assertEquals("java_lang_Double_3 = (double) apache_Fraction_2.intValue()", t.getTest()[66].toString());
		assertEquals("java_lang_Object_2 = apache_Fraction_2.divide(apache_Fraction_2)", t.getTest()[67].toString());
		assertEquals("apache_Fraction_2 = new apache.Fraction(java_lang_Double_2, java_lang_Integer_2)", t.getTest()[68].toString());
		assertEquals("apache_Fraction_2 = apache.Fraction.THREE_FIFTHS", t.getTest()[69].toString());
		assertEquals("apache_Fraction_2 = new apache.Fraction(java_lang_Double_1, java_lang_Integer_3)", t.getTest()[70].toString());
		assertEquals("java_lang_Object_0 = apache.Fraction.TWO", t.getTest()[71].toString());
		assertEquals("java_lang_Object_0 = apache.Fraction.TWO_FIFTHS", t.getTest()[72].toString());
		assertEquals("java_lang_Object_3 = apache.Fraction.ONE_FIFTH", t.getTest()[73].toString());
		assertEquals("apache_Fraction_0.equals(apache_Fraction_0)", t.getTest()[74].toString());
		assertEquals("apache_Fraction_3.equals(apache_Fraction_1)", t.getTest()[75].toString());
		assertEquals("java_lang_Integer_2 = (int)2147483647", t.getTest()[76].toString());
		assertEquals("java_lang_Integer_0 = (int) apache_Fraction_2.getDenominator()", t.getTest()[77].toString());
		assertEquals("java_lang_Integer_3 = (int)6", t.getTest()[78].toString());
		assertEquals("apache_Fraction_3 = apache_Fraction_3.abs()", t.getTest()[79].toString());
		assertEquals("java_lang_Object_0 = apache_Fraction_3.add(java_lang_Integer_2)", t.getTest()[80].toString());
		assertEquals("java_lang_Integer_0 = (int) apache_Fraction_3.doubleValue()", t.getTest()[81].toString());
		assertEquals("java_lang_Object_2 = apache_Fraction_3.subtract(java_lang_Integer_3)", t.getTest()[82].toString());
		assertEquals("java_lang_Object_0 = apache_Fraction_3.add(java_lang_Integer_2)", t.getTest()[83].toString());
		assertEquals("java_lang_Object_0 = apache.Fraction.TWO", t.getTest()[84].toString());
		assertEquals("apache_Fraction_1 = apache_Fraction_3.negate()", t.getTest()[85].toString());
		assertEquals("apache_Fraction_1 = apache_Fraction_3.multiply(java_lang_Integer_1)", t.getTest()[86].toString());
		assertEquals("apache_Fraction_2 = apache_Fraction_2.multiply(apache_Fraction_1)", t.getTest()[87].toString());
		assertEquals("java_lang_Integer_1 = (int) apache_Fraction_0.doubleValue()", t.getTest()[88].toString());
		assertEquals("java_lang_Object_2 = apache.Fraction.TWO_FIFTHS", t.getTest()[89].toString());
		assertEquals("apache_Fraction_2 = apache_Fraction_3.add(apache_Fraction_1)", t.getTest()[90].toString());
		assertEquals("java_lang_Integer_3 = (int)1", t.getTest()[91].toString());
		assertEquals("java_lang_Double_2 = (double) apache_Fraction_3.doubleValue()", t.getTest()[92].toString());
		assertEquals("apache_Fraction_0 = new apache.Fraction(java_lang_Double_1)", t.getTest()[93].toString());
		assertEquals("java_lang_Integer_1 = (int) apache_Fraction_2.intValue()", t.getTest()[94].toString());
		assertEquals("java_lang_Object_2 = apache.Fraction.ONE", t.getTest()[95].toString());
		assertEquals("java_lang_Object_2 = apache.Fraction.THREE_FIFTHS", t.getTest()[96].toString());
		assertEquals("java_lang_Object_3 = apache.Fraction.TWO_THIRDS", t.getTest()[97].toString());
		assertEquals("java_lang_Object_3 = apache.Fraction.MINUS_ONE", t.getTest()[98].toString());
		assertEquals("java_lang_Double_1 = (double) apache_Fraction_3.floatValue()", t.getTest()[99].toString());

		assertEquals( -530830655, t.getTest()[ 0].hashCode());
		assertEquals(       7192, t.getTest()[ 1].hashCode());
		assertEquals( -445177790, t.getTest()[ 2].hashCode());
		assertEquals(  751514707, t.getTest()[ 3].hashCode());
		assertEquals(  184008229, t.getTest()[ 4].hashCode());
		assertEquals( -447841661, t.getTest()[ 5].hashCode());
		assertEquals(-1671287386, t.getTest()[ 6].hashCode());
		assertEquals(  -32021885, t.getTest()[ 7].hashCode());
		assertEquals( 1861195303, t.getTest()[ 8].hashCode());
		assertEquals( -307050836, t.getTest()[ 9].hashCode());
		assertEquals( -522979440, t.getTest()[10].hashCode());
		assertEquals(-1286290892, t.getTest()[11].hashCode());
		assertEquals( 1945972078, t.getTest()[12].hashCode());
		assertEquals(-1090692839, t.getTest()[13].hashCode());
		assertEquals(   26787523, t.getTest()[14].hashCode());
		assertEquals( -671177284, t.getTest()[15].hashCode());
		assertEquals( 1739560627, t.getTest()[16].hashCode());
		assertEquals( -445170660, t.getTest()[17].hashCode());
		assertEquals(-1633939215, t.getTest()[18].hashCode());
		assertEquals(       8989, t.getTest()[19].hashCode());
		assertEquals(  -47634837, t.getTest()[20].hashCode());
		assertEquals( -447843087, t.getTest()[21].hashCode());
		assertEquals( -526119926, t.getTest()[22].hashCode());
		assertEquals( -443397017, t.getTest()[23].hashCode());
		assertEquals( 1755215956, t.getTest()[24].hashCode());
		assertEquals(-1099986980, t.getTest()[25].hashCode());
		assertEquals( -529217973, t.getTest()[26].hashCode());
		assertEquals( 1131663299, t.getTest()[27].hashCode());
		assertEquals( -850507669, t.getTest()[28].hashCode());
		assertEquals( -752947885, t.getTest()[29].hashCode());
		assertEquals(-1928668213, t.getTest()[30].hashCode());
		assertEquals( -286637677, t.getTest()[31].hashCode());
		assertEquals(  523352157, t.getTest()[32].hashCode());
		assertEquals( -447835854, t.getTest()[33].hashCode());
		assertEquals( 1941261349, t.getTest()[34].hashCode());
		assertEquals( 1944232768, t.getTest()[35].hashCode());
		assertEquals( 1758318696, t.getTest()[36].hashCode());
		assertEquals(  450995975, t.getTest()[37].hashCode());
		assertEquals( 1936381553, t.getTest()[38].hashCode());
		assertEquals( -445169947, t.getTest()[39].hashCode());
		assertEquals(  -81157730, t.getTest()[40].hashCode());
		assertEquals(-1274745497, t.getTest()[41].hashCode());
		assertEquals(       7193, t.getTest()[42].hashCode());
		assertEquals(  844040617, t.getTest()[43].hashCode());
		assertEquals(-1928657518, t.getTest()[44].hashCode());
		assertEquals(  905124738, t.getTest()[45].hashCode());
		assertEquals(  -36775022, t.getTest()[46].hashCode());
		assertEquals(  905124025, t.getTest()[47].hashCode());
		assertEquals( 1026175382, t.getTest()[48].hashCode());
		assertEquals(  967885800, t.getTest()[49].hashCode());
		assertEquals(  450997401, t.getTest()[50].hashCode());
		assertEquals(-1826039023, t.getTest()[51].hashCode());
		assertEquals(-1928668213, t.getTest()[52].hashCode());
		assertEquals( 1770960856, t.getTest()[53].hashCode());
		assertEquals(  770310615, t.getTest()[54].hashCode());
		assertEquals(  -33634629, t.getTest()[55].hashCode());
		assertEquals(-1826039178, t.getTest()[56].hashCode());
		assertEquals( -830056640, t.getTest()[57].hashCode());
		assertEquals( -756130810, t.getTest()[58].hashCode());
		assertEquals(  -30404572, t.getTest()[59].hashCode());
		assertEquals(  184008229, t.getTest()[60].hashCode());
		assertEquals( -834767431, t.getTest()[61].hashCode());
		assertEquals(-1092220643, t.getTest()[62].hashCode());
		assertEquals(  184008229, t.getTest()[63].hashCode());
		assertEquals( -447842374, t.getTest()[64].hashCode());
		assertEquals( -896936845, t.getTest()[65].hashCode());
		assertEquals( -529260412, t.getTest()[66].hashCode());
		assertEquals( -812783998, t.getTest()[67].hashCode());
		assertEquals(  361869501, t.getTest()[68].hashCode());
		assertEquals(-1633947058, t.getTest()[69].hashCode());
		assertEquals(  361868571, t.getTest()[70].hashCode());
		assertEquals( -447837993, t.getTest()[71].hashCode());
		assertEquals(-1674709574, t.getTest()[72].hashCode());
		assertEquals(  892223844, t.getTest()[73].hashCode());
		assertEquals(-1826166557, t.getTest()[74].hashCode());
		assertEquals(-1826039209, t.getTest()[75].hashCode());
		assertEquals(-2147474659, t.getTest()[76].hashCode());
		assertEquals(-1093748447, t.getTest()[77].hashCode());
		assertEquals(       9895, t.getTest()[78].hashCode());
		assertEquals( 1735683791, t.getTest()[79].hashCode());
		assertEquals( 1753735284, t.getTest()[80].hashCode());
		assertEquals(  -74791880, t.getTest()[81].hashCode());
		assertEquals(  773498202, t.getTest()[82].hashCode());
		assertEquals( 1753735284, t.getTest()[83].hashCode());
		assertEquals( -447837993, t.getTest()[84].hashCode());
		assertEquals(  643179129, t.getTest()[85].hashCode());
		assertEquals(  -50775323, t.getTest()[86].hashCode());
		assertEquals(  -33549720, t.getTest()[87].hashCode());
		assertEquals(  -73348954, t.getTest()[88].hashCode());
		assertEquals(-1674708148, t.getTest()[89].hashCode());
		assertEquals( 1753730622, t.getTest()[90].hashCode());
		assertEquals(       9890, t.getTest()[91].hashCode());
		assertEquals(  -77932366, t.getTest()[92].hashCode());
		assertEquals(  844040555, t.getTest()[93].hashCode());
		assertEquals( -526119926, t.getTest()[94].hashCode());
		assertEquals( -447841661, t.getTest()[95].hashCode());
		assertEquals(-1633938502, t.getTest()[96].hashCode());
		assertEquals(-1274735515, t.getTest()[97].hashCode());
		assertEquals( -787974947, t.getTest()[98].hashCode());
		assertEquals( 1936508870, t.getTest()[99].hashCode());

		assertEquals( 1621547020, t.hashCode());
	}
}
