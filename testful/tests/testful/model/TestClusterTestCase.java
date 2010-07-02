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

package testful.model;

import java.util.ArrayList;

import testful.ConfigCut;
import testful.GenericTestCase;
import testful.model.TestCluster.MissingClassException;
import testful.runner.TestfulClassLoader;
/**
 * Checks the validity of test clusters.
 *
 * @author matteo
 */
public class TestClusterTestCase extends GenericTestCase {

	public void testArrayList() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut(ArrayList.class.getName());

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		assertEquals(8, tc.getClusterSize());
		assertEquals("java.lang.Integer", tc.getCluster(0).toString());
		assertEquals("java.lang.Object", tc.getCluster(1).toString());
		assertEquals("java.lang.String", tc.getCluster(2).toString());
		assertEquals("java.util.ArrayList", tc.getCluster(3).toString());
		assertEquals("java.util.Collection", tc.getCluster(4).toString());
		assertEquals("java.util.Iterator", tc.getCluster(5).toString());
		assertEquals("java.util.List", tc.getCluster(6).toString());
		assertEquals("java.util.ListIterator", tc.getCluster(7).toString());

		Clazz obj = tc.getCluster(1);
		assertEquals(0, obj.getMethods().length);

		Clazz iter = tc.getCluster(5);
		assertEquals(3, iter.getMethods().length);
		assertEquals("hasNext()", iter.getMethods()[0].toString());
		assertEquals("next()", iter.getMethods()[1].toString());
		assertEquals("remove()", iter.getMethods()[2].toString());
	}

	/**
	 * Test the ability to skip constructors and methods'
	 */
	public void testRandomIgnore() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("java.util.Random");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		assertNotNull(tc.getCut());

		// checking that java.util.Random() is not present, while other constructors are present
		assertNotNull(tc.getCut().getConstructors());
		assertEquals(1, tc.getCut().getConstructors().length);
		assertEquals("java.util.Random(long)", tc.getCut().getConstructors()[0].toString());

		// checking that java.util.Random.nextInt() is not present, while other methods are present
		assertNotNull(tc.getCut().getMethods());
		assertEquals(7, tc.getCut().getMethods().length);
		assertEquals("nextBoolean()", tc.getCut().getMethods()[0].toString());
		assertEquals("nextDouble()", tc.getCut().getMethods()[1].toString());
		assertEquals("nextFloat()", tc.getCut().getMethods()[2].toString());
		assertEquals("nextGaussian()", tc.getCut().getMethods()[3].toString());
		assertEquals("nextInt()", tc.getCut().getMethods()[4].toString());
		assertEquals("nextLong()", tc.getCut().getMethods()[5].toString());
		assertEquals("setSeed(long)", tc.getCut().getMethods()[6].toString());
	}

	public void test01() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test01.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test02() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test02.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
			fail("The cluster is not valid.");
		} catch (MissingClassException e) {
			assertEquals(1, e.missing.size());
			assertTrue(e.missing.contains("test.model.cluster.test02.I"));
		}
	}

	public void test03() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test03.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test04() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test04.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
			fail("The cluster is not valid.");
		} catch (MissingClassException e) {
			assertEquals(1, e.missing.size());
			assertTrue(e.missing.contains("test.model.cluster.test04.I"));
		}
	}

	public void test05() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test05.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test10() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test10.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test11() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test11.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test12() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test12.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test13() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test13.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test14() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test14.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test15() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test15.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test16() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test16.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test17() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test17.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	// TODO: solve test 18
	//	public void test18() throws Exception {
	//		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
	//		config.setCut("test.model.cluster.test18.Cut");
	//
	//		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);
	//
	//		try {
	//			tc.isValid();
	//			fail("The cluster is not valid.");
	//		} catch (MissingClassException e) {
	//			assertTrue(e.fatal);
	//			assertEquals(1, e.missing.size());
	//			assertTrue(e.missing.contains("test.model.cluster.test18.I"));
	//		}
	//	}

	public void test19() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test19.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	// TODO: solve test 20
	//	public void test20() throws Exception {
	//		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
	//		config.setCut("test.model.cluster.test20.Cut");
	//
	//		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);
	//
	//		try {
	//			tc.isValid();
	//			fail("The cluster is not valid.");
	//		} catch (MissingClassException e) {
	//			assertTrue(e.fatal);
	//			assertEquals(1, e.missing.size());
	//			assertTrue(e.missing.contains("test.model.cluster.test20.I"));
	//		}
	//	}

	public void test21() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test21.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	// TODO: work on test 22
	//	public void test22() throws Exception {
	//		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
	//		config.setCut("test.model.cluster.test22.Cut");
	//
	//		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);
	//
	//		try {
	//			tc.isValid();
	//			fail("The cluster is not valid.");
	//		} catch (MissingClassException e) {
	//			assertTrue(e.fatal);
	//			assertEquals(1, e.missing.size());
	//			assertTrue(e.missing.contains("test.model.cluster.test22.I"));
	//		}
	//	}

	public void test23() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test23.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test24() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test24.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test25() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test25.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	// TODO: work on test 26
	//	public void test26() throws Exception {
	//		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
	//		config.setCut("test.model.cluster.test26.Cut");
	//
	//		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);
	//
	//		try {
	//			tc.isValid();
	//			fail("The cluster is not valid.");
	//		} catch (MissingClassException e) {
	//			assertTrue(e.fatal);
	//			assertEquals(1, e.missing.size());
	//			assertTrue(e.missing.contains("test.model.cluster.test26.I"));
	//		}
	//	}

	public void test27() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test27.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test28() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test28.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test29() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test29.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	//TODO: work on test 30
	//	public void test30() throws Exception {
	//		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
	//		config.setCut("test.model.cluster.test30.Cut");
	//
	//		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);
	//
	//		try {
	//			tc.isValid();
	//			fail("The cluster is not valid.");
	//		} catch (MissingClassException e) {
	//			assertTrue(e.fatal);
	//			assertEquals(1, e.missing.size());
	//			assertTrue(e.missing.contains("test.model.cluster.test30.I"));
	//		}
	//	}

	public void test31() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test31.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test32() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test32.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test33() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test33.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	//TODO: work on test 34
	//	public void test34() throws Exception {
	//		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
	//		config.setCut("test.model.cluster.test34.Cut");
	//
	//		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);
	//
	//		try {
	//			tc.isValid();
	//			fail("The cluster is not valid.");
	//		} catch (MissingClassException e) {
	//			assertTrue(e.fatal);
	//			assertEquals(1, e.missing.size());
	//			assertTrue(e.missing.contains("test.model.cluster.test34.I"));
	//		}
	//	}

	public void test35() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test35.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void test36() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.test36.Cut");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	//	public void test37() throws Exception {
	//		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
	//		config.setCut("test.model.cluster.test37.Cut");
	//
	//		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);
	//
	//		try {
	//			tc.isValid();
	//		} catch (MissingClassException e) {
	//			fail(e.getMessage());
	//		}
	//	}
}