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

import java.util.Arrays;

import testful.ApacheFractionTestCase;
import testful.GenericTestCase;

/**
 * Ensures that Testful has a repeatable behavior
 * @author matteo
 */
public class RepeatableTestCase extends ApacheFractionTestCase {

	/* (non-Javadoc)
	 * @see testful.ApacheFractionTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testCluster() throws Exception {
		String[] classes = cluster.getClasses();

		System.out.println(Arrays.toString(classes));

		assertEquals(4, classes.length);
		assertEquals("apache.Fraction", classes[0]);
		assertEquals("java.lang.Double", classes[1]);
		assertEquals("java.lang.Integer", classes[2]);
		assertEquals("java.lang.Object", classes[3]);
	}

	public void testReferenceFactory() throws Exception {
		Reference[] refs = refFactory.getReferences();

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
		Operation[] ops = GenericTestCase.createRandomTest("apache.Fraction", 10, 12012010l).getTest();

		//	for (int i = 0; i < ops.length; i++)
		//		System.out.println("assertEquals(\"" + ops[i].toString() + "\", ops[" + i + "].toString());");

		assertEquals("apache_Fraction_2 = apache_Fraction_0.reciprocal()", ops[0].toString());
		assertEquals("java_lang_Integer_0 = (int)0", ops[1].toString());
		assertEquals("apache_Fraction_3 = apache.Fraction.ZERO", ops[2].toString());
		assertEquals("apache_Fraction_0 = apache_Fraction_2.add(java_lang_Integer_0)", ops[3].toString());
		assertEquals("java_lang_Object_1 = new java.lang.Object()", ops[4].toString());
		assertEquals("java_lang_Object_2 = apache.Fraction.ONE", ops[5].toString());
		assertEquals("apache_Fraction_2 = apache.Fraction.FOUR_FIFTHS", ops[6].toString());
		assertEquals("java_lang_Integer_2 = (int) apache_Fraction_1.floatValue()", ops[7].toString());
		assertEquals("java_lang_Object_0 = apache.Fraction.THREE_QUARTERS", ops[8].toString());
		assertEquals("java_lang_Object_0 = apache.Fraction.MINUS_ONE", ops[9].toString());
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
		Test t = new Test(cluster, refFactory, new Operation[] {
				new AssignPrimitive(doubles[3], -0.4445457912686642),
				new AssignPrimitive(ints[2], 5),
				new CreateObject(cuts[0], cns_double_int, new Reference[] { doubles[3], ints[2] }),
				new Invoke(null, cuts[0], divide_Fraction, new Reference[] { cuts[0] } )
		});

		assertEquals(-1894196417, t.getTest()[0].hashCode());
		assertEquals(       8995, t.getTest()[1].hashCode());
		assertEquals(  361810880, t.getTest()[2].hashCode());
		assertEquals( -834852340, t.getTest()[3].hashCode());
		assertEquals( -924205935, t.hashCode());
	}

	public void testHashCodeLong() throws Exception {
		Test t1 = GenericTestCase.createRandomTest("apache.Fraction", 100, 12012010l);

		//	Operation[] t = t1.getTest();
		//	for (int i = 0; i < t.length; i++)
		//		System.out.printf("assertEquals(%11d, t1.getTest()[" +  i + "].hashCode());\n", t[i].hashCode());

		assertEquals( -305480593, t1.getTest()[0].hashCode());
		assertEquals(       7192, t1.getTest()[1].hashCode());
		assertEquals( -445177790, t1.getTest()[2].hashCode());
		assertEquals( 1734849867, t1.getTest()[3].hashCode());
		assertEquals(  184008229, t1.getTest()[4].hashCode());
		assertEquals( -447841661, t1.getTest()[5].hashCode());
		assertEquals(-1671287386, t1.getTest()[6].hashCode());
		assertEquals( 1944275207, t1.getTest()[7].hashCode());
		assertEquals( 1861203859, t1.getTest()[8].hashCode());
		assertEquals( -787977086, t1.getTest()[9].hashCode());
		assertEquals(  -76404562, t1.getTest()[10].hashCode());
		assertEquals(-1674707435, t1.getTest()[11].hashCode());
		assertEquals(       7194, t1.getTest()[12].hashCode());
		assertEquals( -756130810, t1.getTest()[13].hashCode());
		assertEquals(  450995975, t1.getTest()[14].hashCode());
		assertEquals( 1072696844, t1.getTest()[15].hashCode());
		assertEquals( -675845574, t1.getTest()[16].hashCode());
		assertEquals(       4495, t1.getTest()[17].hashCode());
		assertEquals( 1072697743, t1.getTest()[18].hashCode());
		assertEquals(-1591033388, t1.getTest()[19].hashCode());
		assertEquals(-1633939215, t1.getTest()[20].hashCode());
		assertEquals(       8989, t1.getTest()[21].hashCode());
		assertEquals(  -73264076, t1.getTest()[22].hashCode());
		assertEquals( -848979834, t1.getTest()[23].hashCode());
		assertEquals( -288123042, t1.getTest()[24].hashCode());
		assertEquals( -897206018, t1.getTest()[25].hashCode());
		assertEquals( 1830254731, t1.getTest()[26].hashCode());
		assertEquals( -447844410, t1.getTest()[27].hashCode());
		assertEquals(       2139, t1.getTest()[28].hashCode());
		assertEquals( 1072696844, t1.getTest()[29].hashCode());
		assertEquals(  665035214, t1.getTest()[30].hashCode());
		assertEquals(  184008229, t1.getTest()[31].hashCode());
		assertEquals(-1674708148, t1.getTest()[32].hashCode());
		assertEquals( 2146963855, t1.getTest()[33].hashCode());
		assertEquals( 1755482686, t1.getTest()[34].hashCode());
		assertEquals( 1752956464, t1.getTest()[35].hashCode());
		assertEquals( 1072698642, t1.getTest()[36].hashCode());
		assertEquals( -787984216, t1.getTest()[37].hashCode());
		assertEquals(  183978438, t1.getTest()[38].hashCode());
		assertEquals(  -71693833, t1.getTest()[39].hashCode());
		assertEquals(-1826123684, t1.getTest()[40].hashCode());
		assertEquals(  184038020, t1.getTest()[41].hashCode());
		assertEquals(       9888, t1.getTest()[42].hashCode());
		assertEquals( 1072697743, t1.getTest()[43].hashCode());
		assertEquals(-1674708861, t1.getTest()[44].hashCode());
		assertEquals( -997601657, t1.getTest()[45].hashCode());
		assertEquals(       7193, t1.getTest()[46].hashCode());
		assertEquals(  844040617, t1.getTest()[47].hashCode());
		assertEquals(-1928657518, t1.getTest()[48].hashCode());
		assertEquals(  905124738, t1.getTest()[49].hashCode());
		assertEquals(  665077653, t1.getTest()[50].hashCode());
		assertEquals(  -35204810, t1.getTest()[51].hashCode());
		assertEquals(  967843361, t1.getTest()[52].hashCode());
		assertEquals( -675845574, t1.getTest()[53].hashCode());
		assertEquals(  450997401, t1.getTest()[54].hashCode());
		assertEquals( -303910350, t1.getTest()[55].hashCode());
		assertEquals(-2147474658, t1.getTest()[56].hashCode());
		assertEquals( 1830297170, t1.getTest()[57].hashCode());
		assertEquals(  464527566, t1.getTest()[58].hashCode());
		assertEquals( -308536201, t1.getTest()[59].hashCode());
		assertEquals(       9897, t1.getTest()[60].hashCode());
		assertEquals( -787974947, t1.getTest()[61].hashCode());
		assertEquals( 1939691106, t1.getTest()[62].hashCode());
		assertEquals(    1054869, t1.getTest()[63].hashCode());
		assertEquals(-1826081276, t1.getTest()[64].hashCode());
		assertEquals(  644664494, t1.getTest()[65].hashCode());
		assertEquals( -534013580, t1.getTest()[66].hashCode());
		assertEquals( 1734849960, t1.getTest()[67].hashCode());
		assertEquals( -447842374, t1.getTest()[68].hashCode());
		assertEquals( -997244134, t1.getTest()[69].hashCode());
		assertEquals( -445169947, t1.getTest()[70].hashCode());
		assertEquals(       8992, t1.getTest()[71].hashCode());
		assertEquals(  361869501, t1.getTest()[72].hashCode());
		assertEquals(-1633947058, t1.getTest()[73].hashCode());
		assertEquals(  361868571, t1.getTest()[74].hashCode());
		assertEquals( -447837993, t1.getTest()[75].hashCode());
		assertEquals(-1674709574, t1.getTest()[76].hashCode());
		assertEquals(  892223844, t1.getTest()[77].hashCode());
		assertEquals( -850422822, t1.getTest()[78].hashCode());
		assertEquals(  -76489440, t1.getTest()[79].hashCode());
		assertEquals( 1072699541, t1.getTest()[80].hashCode());
		assertEquals( -527732608, t1.getTest()[81].hashCode());
		assertEquals(-1274744071, t1.getTest()[82].hashCode());
		assertEquals(  462914884, t1.getTest()[83].hashCode());
		assertEquals( -672620210, t1.getTest()[84].hashCode());
		assertEquals( 1949112564, t1.getTest()[85].hashCode());
		assertEquals(  -74919197, t1.getTest()[86].hashCode());
		assertEquals( -812868845, t1.getTest()[87].hashCode());
		assertEquals( 1938036674, t1.getTest()[88].hashCode());
		assertEquals(  -71778711, t1.getTest()[89].hashCode());
		assertEquals( -445178503, t1.getTest()[90].hashCode());
		assertEquals( 1934811310, t1.getTest()[91].hashCode());
		assertEquals(  715512916, t1.getTest()[92].hashCode());
		assertEquals(-2147474658, t1.getTest()[93].hashCode());
		assertEquals(-1590948510, t1.getTest()[94].hashCode());
		assertEquals(  184067811, t1.getTest()[95].hashCode());
		assertEquals( 1945929639, t1.getTest()[96].hashCode());
		assertEquals(  661894728, t1.getTest()[97].hashCode());
		assertEquals(  963132632, t1.getTest()[98].hashCode());
		assertEquals( 1936508870, t1.getTest()[99].hashCode());

		assertEquals( -807758438, t1.hashCode());
	}
}
