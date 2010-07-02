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
		String[] classes = cut.cluster.getClasses();

		assertEquals(4, classes.length);
		assertEquals("apache.Fraction", classes[0]);
		assertEquals("java.lang.Double", classes[1]);
		assertEquals("java.lang.Integer", classes[2]);
		assertEquals("java.lang.Object", classes[3]);
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
		Operation[] ops = GenericTestCase.createRandomTest("apache.Fraction", 10, 12012010l).getTest();

		assertEquals(10, ops.length);
		assertEquals("java_lang_Double_2 = (double) apache_Fraction_2.floatValue()", ops[0].toString());
		assertEquals("java_lang_Integer_0 = (int)0", ops[1].toString());
		assertEquals("apache_Fraction_3 = apache.Fraction.ZERO", ops[2].toString());
		assertEquals("java_lang_Integer_0 = (int) apache_Fraction_3.getDenominator()", ops[3].toString());
		assertEquals("java_lang_Object_0 = apache_Fraction_2.reciprocal()", ops[4].toString());
		assertEquals("java_lang_Object_3 = apache.Fraction.THREE_QUARTERS", ops[5].toString());
		assertEquals("apache_Fraction_1 = apache_Fraction_0.negate()", ops[6].toString());
		assertEquals("java_lang_Double_0 = (double)0.0", ops[7].toString());
		assertEquals("apache_Fraction_0 = apache.Fraction.THREE_QUARTERS", ops[8].toString());
		assertEquals("java_lang_Object_3 = apache_Fraction_0.add(apache_Fraction_1)", ops[9].toString());
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

		assertEquals( 1938036674, t.getTest()[ 0].hashCode());
		assertEquals(       7192, t.getTest()[ 1].hashCode());
		assertEquals( -445177790, t.getTest()[ 2].hashCode());
		assertEquals(-1093706008, t.getTest()[ 3].hashCode());
		assertEquals( -289693285, t.getTest()[ 4].hashCode());
		assertEquals( 1861205998, t.getTest()[ 5].hashCode());
		assertEquals(  643051812, t.getTest()[ 6].hashCode());
		assertEquals(       3596, t.getTest()[ 7].hashCode());
		assertEquals( 1861195303, t.getTest()[ 8].hashCode());
		assertEquals( 1774016464, t.getTest()[ 9].hashCode());
		assertEquals(  905125451, t.getTest()[10].hashCode());
		assertEquals( -447841661, t.getTest()[11].hashCode());
		assertEquals(  892222418, t.getTest()[12].hashCode());
		assertEquals(  663464971, t.getTest()[13].hashCode());
		assertEquals(   26787523, t.getTest()[14].hashCode());
		assertEquals( -523064318, t.getTest()[15].hashCode());
		assertEquals(-1590948510, t.getTest()[16].hashCode());
		assertEquals( 1072697743, t.getTest()[17].hashCode());
		assertEquals( -847409560, t.getTest()[18].hashCode());
		assertEquals( 1830254731, t.getTest()[19].hashCode());
		assertEquals( 1756790861, t.getTest()[20].hashCode());
		assertEquals(-1096888933, t.getTest()[21].hashCode());
		assertEquals( -760799100, t.getTest()[22].hashCode());
		assertEquals(-1826039209, t.getTest()[23].hashCode());
		assertEquals( 1830212292, t.getTest()[24].hashCode());
		assertEquals( -447844410, t.getTest()[25].hashCode());
		assertEquals(       2139, t.getTest()[26].hashCode());
		assertEquals( 1131663299, t.getTest()[27].hashCode());
		assertEquals(  -36732645, t.getTest()[28].hashCode());
		assertEquals(-1590948510, t.getTest()[29].hashCode());
		assertEquals(       9880, t.getTest()[30].hashCode());
		assertEquals( -284940117, t.getTest()[31].hashCode());
		assertEquals(  -80929631, t.getTest()[32].hashCode());
		assertEquals(  523352157, t.getTest()[33].hashCode());
		assertEquals( -447835854, t.getTest()[34].hashCode());
		assertEquals(  923084955, t.getTest()[35].hashCode());
		assertEquals( 1750462850, t.getTest()[36].hashCode());
		assertEquals(  775026006, t.getTest()[37].hashCode());
		assertEquals(-1237527126, t.getTest()[38].hashCode());
		assertEquals(  183978438, t.getTest()[39].hashCode());
		assertEquals( 1774101311, t.getTest()[40].hashCode());
		assertEquals(          0, t.getTest()[41].hashCode());
		assertEquals( 1754441829, t.getTest()[42].hashCode());
		assertEquals( 1072696844, t.getTest()[43].hashCode());
		assertEquals( -674317770, t.getTest()[44].hashCode());
		assertEquals(       3596, t.getTest()[45].hashCode());
		assertEquals(-1826166371, t.getTest()[46].hashCode());
		assertEquals(-2147475558, t.getTest()[47].hashCode());
		assertEquals(-1096973811, t.getTest()[48].hashCode());
		assertEquals( -533971141, t.getTest()[49].hashCode());
		assertEquals( -674317770, t.getTest()[50].hashCode());
		assertEquals(  -13178938, t.getTest()[51].hashCode());
		assertEquals(       8101, t.getTest()[52].hashCode());
		assertEquals(-1928668213, t.getTest()[53].hashCode());
		assertEquals( -526119926, t.getTest()[54].hashCode());
		assertEquals( 1941303788, t.getTest()[55].hashCode());
		assertEquals( -675888013, t.getTest()[56].hashCode());
		assertEquals(-1633947058, t.getTest()[57].hashCode());
		assertEquals(-1674708861, t.getTest()[58].hashCode());
		assertEquals( -787974947, t.getTest()[59].hashCode());
		assertEquals(-1826166526, t.getTest()[60].hashCode());
		assertEquals( -760799100, t.getTest()[61].hashCode());
		assertEquals(       8090, t.getTest()[62].hashCode());
		assertEquals(  -52388005, t.getTest()[63].hashCode());
		assertEquals( -811256225, t.getTest()[64].hashCode());
		assertEquals( -447850930, t.getTest()[65].hashCode());
		assertEquals( 1753645775, t.getTest()[66].hashCode());
		assertEquals( -896936845, t.getTest()[67].hashCode());
		assertEquals(  770315277, t.getTest()[68].hashCode());
		assertEquals(-1274737654, t.getTest()[69].hashCode());
		assertEquals(-2147476457, t.getTest()[70].hashCode());
		assertEquals(  -32017316, t.getTest()[71].hashCode());
		assertEquals( -447837993, t.getTest()[72].hashCode());
		assertEquals(-1674709574, t.getTest()[73].hashCode());
		assertEquals(  892223844, t.getTest()[74].hashCode());
		assertEquals( 1934811310, t.getTest()[75].hashCode());
		assertEquals(  450988845, t.getTest()[76].hashCode());
		assertEquals(-2147474659, t.getTest()[77].hashCode());
		assertEquals( 1830254731, t.getTest()[78].hashCode());
		assertEquals( -834767400, t.getTest()[79].hashCode());
		assertEquals(-1674707435, t.getTest()[80].hashCode());
		assertEquals(  892223131, t.getTest()[81].hashCode());
		assertEquals(  929450805, t.getTest()[82].hashCode());
		assertEquals( 1030970989, t.getTest()[83].hashCode());
		assertEquals( -757616175, t.getTest()[84].hashCode());
		assertEquals( 1756875801, t.getTest()[85].hashCode());
		assertEquals( 1753735284, t.getTest()[86].hashCode());
		assertEquals( -447837993, t.getTest()[87].hashCode());
		assertEquals( 1830297170, t.getTest()[88].hashCode());
		assertEquals( 1020021727, t.getTest()[89].hashCode());
		assertEquals(  715512916, t.getTest()[90].hashCode());
		assertEquals(-2147474658, t.getTest()[91].hashCode());
		assertEquals(  472378812, t.getTest()[92].hashCode());
		assertEquals(  636697037, t.getTest()[93].hashCode());
		assertEquals(-1674708148, t.getTest()[94].hashCode());
		assertEquals(  754655224, t.getTest()[95].hashCode());
		assertEquals(       9890, t.getTest()[96].hashCode());
		assertEquals( 1734113548, t.getTest()[97].hashCode());
		assertEquals(  844040555, t.getTest()[98].hashCode());
		assertEquals(-1826081276, t.getTest()[99].hashCode());

		assertEquals( -927511277, t.hashCode());
	}
}
