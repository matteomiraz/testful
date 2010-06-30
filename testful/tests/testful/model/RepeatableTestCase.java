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

		assertEquals("apache_Fraction_2 = apache_Fraction_2.add(java_lang_Integer_2)", ops[0].toString());
		assertEquals("apache_Fraction_0 = null", ops[1].toString());
		assertEquals("apache_Fraction_3 = apache.Fraction.ZERO", ops[2].toString());
		assertEquals("java_lang_Object_0 = apache_Fraction_3.negate()", ops[3].toString());
		assertEquals("apache_Fraction_0 = apache_Fraction_2.subtract(java_lang_Integer_0)", ops[4].toString());
		assertEquals("java_lang_Integer_0 = (int) apache_Fraction_2.hashCode()", ops[5].toString());
		assertEquals("java_lang_Object_3 = apache.Fraction.ONE", ops[6].toString());
		assertEquals("java_lang_Integer_1 = (int)-2147483648", ops[7].toString());
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


		assertEquals( 1737990415, t.getTest()[ 0].hashCode());
		assertEquals(          0, t.getTest()[ 1].hashCode());
		assertEquals( -445177790, t.getTest()[ 2].hashCode());
		assertEquals(  660451802, t.getTest()[ 3].hashCode());
		assertEquals(  751472268, t.getTest()[ 4].hashCode());
		assertEquals( -757658614, t.getTest()[ 5].hashCode());
		assertEquals( -447840948, t.getTest()[ 6].hashCode());
		assertEquals(-2147475557, t.getTest()[ 7].hashCode());
		assertEquals( 1861203859, t.getTest()[ 8].hashCode());
		assertEquals( -787977086, t.getTest()[ 9].hashCode());
		assertEquals( 1942916470, t.getTest()[10].hashCode());
		assertEquals(-1674707435, t.getTest()[11].hashCode());
		assertEquals(       7194, t.getTest()[12].hashCode());
		assertEquals( -288207920, t.getTest()[13].hashCode());
		assertEquals(  450995975, t.getTest()[14].hashCode());
		assertEquals(   26787523, t.getTest()[15].hashCode());
		assertEquals( -847409560, t.getTest()[16].hashCode());
		assertEquals( 1739560627, t.getTest()[17].hashCode());
		assertEquals( -445170660, t.getTest()[18].hashCode());
		assertEquals(-1633939215, t.getTest()[19].hashCode());
		assertEquals(       8989, t.getTest()[20].hashCode());
		assertEquals( -756045932, t.getTest()[21].hashCode());
		assertEquals(  768740372, t.getTest()[22].hashCode());
		assertEquals( -756088371, t.getTest()[23].hashCode());
		assertEquals( -443397017, t.getTest()[24].hashCode());
		assertEquals( 1749731100, t.getTest()[25].hashCode());
		assertEquals(-1089080157, t.getTest()[26].hashCode());
		assertEquals(-1928666074, t.getTest()[27].hashCode());
		assertEquals( -831622252, t.getTest()[28].hashCode());
		assertEquals(  184008229, t.getTest()[29].hashCode());
		assertEquals(-1674708148, t.getTest()[30].hashCode());
		assertEquals( 2146963855, t.getTest()[31].hashCode());
		assertEquals(       8996, t.getTest()[32].hashCode());
		assertEquals( 1944232768, t.getTest()[33].hashCode());
		assertEquals(  523352157, t.getTest()[34].hashCode());
		assertEquals( -447835854, t.getTest()[35].hashCode());
		assertEquals(-1826123963, t.getTest()[36].hashCode());
		assertEquals(       8982, t.getTest()[37].hashCode());
		assertEquals( 1754484268, t.getTest()[38].hashCode());
		assertEquals(  450995975, t.getTest()[39].hashCode());
		assertEquals( 1939648667, t.getTest()[40].hashCode());
		assertEquals( -445169947, t.getTest()[41].hashCode());
		assertEquals( -850507700, t.getTest()[42].hashCode());
		assertEquals( -447836567, t.getTest()[43].hashCode());
		assertEquals( -997303685, t.getTest()[44].hashCode());
		assertEquals( -674317770, t.getTest()[45].hashCode());
		assertEquals(       3596, t.getTest()[46].hashCode());
		assertEquals(  770225675, t.getTest()[47].hashCode());
		assertEquals(-2147475558, t.getTest()[48].hashCode());
		assertEquals(  -35204810, t.getTest()[49].hashCode());
		assertEquals( 1944359396, t.getTest()[50].hashCode());
		assertEquals(  472336280, t.getTest()[51].hashCode());
		assertEquals(  -82021238, t.getTest()[52].hashCode());
		assertEquals(  775068445, t.getTest()[53].hashCode());
		assertEquals(-1928668213, t.getTest()[54].hashCode());
		assertEquals(  469195825, t.getTest()[55].hashCode());
		assertEquals( -530830655, t.getTest()[56].hashCode());
		assertEquals(  -36775115, t.getTest()[57].hashCode());
		assertEquals(       9897, t.getTest()[58].hashCode());
		assertEquals( -787974947, t.getTest()[59].hashCode());
		assertEquals( 1770875978, t.getTest()[60].hashCode());
		assertEquals( 1772531130, t.getTest()[61].hashCode());
		assertEquals(  184008229, t.getTest()[62].hashCode());
		assertEquals(  461344610, t.getTest()[63].hashCode());
		assertEquals( 1758361104, t.getTest()[64].hashCode());
		assertEquals( -447850930, t.getTest()[65].hashCode());
		assertEquals(-1096931372, t.getTest()[66].hashCode());
		assertEquals( -896936845, t.getTest()[67].hashCode());
		assertEquals( 1753692876, t.getTest()[68].hashCode());
		assertEquals(-1274737654, t.getTest()[69].hashCode());
		assertEquals(-2147476457, t.getTest()[70].hashCode());
		assertEquals(-1826124087, t.getTest()[71].hashCode());
		assertEquals( -447837993, t.getTest()[72].hashCode());
		assertEquals(-1674709574, t.getTest()[73].hashCode());
		assertEquals(  892223844, t.getTest()[74].hashCode());
		assertEquals(  767085189, t.getTest()[75].hashCode());
		assertEquals( -303783033, t.getTest()[76].hashCode());
		assertEquals(  110587819, t.getTest()[77].hashCode());
		assertEquals(-1826166309, t.getTest()[78].hashCode());
		assertEquals(       9895, t.getTest()[79].hashCode());
		assertEquals( -529217973, t.getTest()[80].hashCode());
		assertEquals( -672620210, t.getTest()[81].hashCode());
		assertEquals(  786013076, t.getTest()[82].hashCode());
		assertEquals( 2146440466, t.getTest()[83].hashCode());
		assertEquals(-2147475558, t.getTest()[84].hashCode());
		assertEquals(  467668052, t.getTest()[85].hashCode());
		assertEquals( -447837993, t.getTest()[86].hashCode());
		assertEquals( -306923519, t.getTest()[87].hashCode());
		assertEquals(-1826038837, t.getTest()[88].hashCode());
		assertEquals(  -49205049, t.getTest()[89].hashCode());
		assertEquals(-2147474658, t.getTest()[90].hashCode());
		assertEquals(-1088995279, t.getTest()[91].hashCode());
		assertEquals( -526204804, t.getTest()[92].hashCode());
		assertEquals(-1674708148, t.getTest()[93].hashCode());
		assertEquals(  662022045, t.getTest()[94].hashCode());
		assertEquals( 1936423992, t.getTest()[95].hashCode());
		assertEquals(  -79629926, t.getTest()[96].hashCode());
		assertEquals( 1941346227, t.getTest()[97].hashCode());
		assertEquals(  844040555, t.getTest()[98].hashCode());
		assertEquals(  771880827, t.getTest()[99].hashCode());

		assertEquals(  849524984, t.hashCode());
	}
}
