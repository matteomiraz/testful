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

import junit.framework.TestCase;
import testful.ConfigCut;
import testful.GenericTestCase;
import testful.runner.TestfulClassLoader;

/**
 * Ensures that Testful has a repeatable behavior
 * @author matteo
 */
public class RepeatableTestCase extends TestCase {

	public void testCluster() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("apache.Fraction");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(GenericTestCase.getFinder()), config);

		String[] classes = cluster.getClasses();

		assertEquals(4, classes.length);
		assertEquals("apache.Fraction", classes[0]);
		assertEquals("java.lang.Double", classes[1]);
		assertEquals("java.lang.Integer", classes[2]);
		assertEquals("java.lang.Object", classes[3]);
	}

	public void testReferenceFactory() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("apache.Fraction");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(GenericTestCase.getFinder()), config);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 4, 4);

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

		assertEquals("java_lang_Double_2 = (double) apache_Fraction_0.getNumerator()", ops[0].toString());
		assertEquals("java_lang_Integer_0 = (int)0", ops[1].toString());
		assertEquals("apache_Fraction_3 = apache.Fraction.ZERO", ops[2].toString());
		assertEquals("apache_Fraction_0 = apache_Fraction_2.multiply(apache_Fraction_0)", ops[3].toString());
		assertEquals("java_lang_Object_1 = new java.lang.Object()", ops[4].toString());
		assertEquals("java_lang_Object_2 = apache.Fraction.ONE", ops[5].toString());
		assertEquals("apache_Fraction_2 = apache.Fraction.FOUR_FIFTHS", ops[6].toString());
		assertEquals("apache_Fraction_3 = apache_Fraction_1.add(java_lang_Integer_2)", ops[7].toString());
		assertEquals("apache_Fraction_0 = apache.Fraction.THREE_QUARTERS", ops[8].toString());
		assertEquals("java_lang_Double_1 = (double) apache_Fraction_1.getDenominator()", ops[9].toString());
	}

	public void testRandomGenerationExtended() throws Exception {
		Operation[] ops1 = GenericTestCase.createRandomTest("apache.Fraction", 1000, 12012010l).getTest();
		Operation[] ops2 = GenericTestCase.createRandomTest("apache.Fraction", 1000, 12012010l).getTest();

		assertEquals(1000, ops1.length);
		assertEquals(1000, ops2.length);

		for (int i = 0; i < 1000; i++)
			assertEquals(ops1[i], ops2[i]);
	}


}
