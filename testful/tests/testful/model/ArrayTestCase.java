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

import testful.ConfigCut;
import testful.GenericTestCase;
import testful.runner.RemoteClassLoader;

public class ArrayTestCase extends GenericTestCase {

	public void testCluster() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.Array");

		TestClusterBuilder clusterBuilder = new TestClusterBuilder(new RemoteClassLoader(GenericTestCase.getFinder()), config);
		TestCluster cluster = clusterBuilder.getTestCluster();

		// cluster.getCluster()
		assertEquals(3, cluster.getCluster().length);
		assertEquals("java.lang.Integer", cluster.getCluster()[0].toString());
		assertEquals("test.model.Array", cluster.getCluster()[1].toString());
		assertEquals("testful.intArray", cluster.getCluster()[2].toString());

		// cluster.getCut().getConstructors()
		assertEquals(2, cluster.getCut().getConstructors().length);
		assertEquals("test.model.Array()", cluster.getCut().getConstructors()[0].toString());
		assertEquals("test.model.Array(testful.intArray)", cluster.getCut().getConstructors()[1].toString());

		// cluster.getCut().getMethods()
		assertEquals(8, cluster.getCut().getMethods().length);
		assertEquals("m1(testful.intArray)", cluster.getCut().getMethods()[0].toString());
		assertEquals("m2(int)", cluster.getCut().getMethods()[1].toString());
		assertEquals("m2_testful(int)", cluster.getCut().getMethods()[2].toString());
		assertEquals("m3(testful.intArray)", cluster.getCut().getMethods()[3].toString());
		assertEquals("m4(testful.intArray)", cluster.getCut().getMethods()[4].toString());
		assertEquals("m5(int)", cluster.getCut().getMethods()[5].toString());
		assertEquals("m5_testful(int)", cluster.getCut().getMethods()[6].toString());
		assertEquals("m6(testful.intArray)", cluster.getCut().getMethods()[7].toString());

		// cluster.getCut().getConstants()
		assertEquals(1, cluster.getCut().getConstants().length);
		assertEquals("test.model.Array.a", cluster.getCut().getConstants()[0].toString());

		Clazz intArray = cluster.getCluster()[2];
		// intArray.getConstructors()
		assertEquals(2, intArray.getConstructors().length);
		assertEquals("testful.intArray()", intArray.getConstructors()[0].toString());
		assertEquals("testful.intArray(testful.intArray)", intArray.getConstructors()[1].toString());

		// intArray.getMethods()
		assertEquals(9, intArray.getMethods().length);
		assertEquals("addHead(int)", intArray.getMethods()[0].toString());
		assertEquals("addTail(int)", intArray.getMethods()[1].toString());
		assertEquals("delHead()", intArray.getMethods()[2].toString());
		assertEquals("delTail()", intArray.getMethods()[3].toString());
		assertEquals("set(int, int)", intArray.getMethods()[4].toString());
		assertEquals("setHead(int)", intArray.getMethods()[5].toString());
		assertEquals("setTail(int)", intArray.getMethods()[6].toString());
		assertEquals("toArray()", intArray.getMethods()[7].toString());
		assertEquals("toArray_testful()", intArray.getMethods()[8].toString());

		// intArray.getConstants()
		assertEquals(0, intArray.getConstants().length);
	}
}
