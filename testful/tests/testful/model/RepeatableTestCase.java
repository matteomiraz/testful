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
