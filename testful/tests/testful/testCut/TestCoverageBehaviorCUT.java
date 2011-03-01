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

package testful.testCut;

import static junit.framework.Assert.assertEquals;
import testful.coverage.behavior.AbstractorRegistry;
import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.Methodz;
import testful.model.Reference;

public class TestCoverageBehaviorCUT extends SingleTestCUT {

	public final AbstractorRegistry abstractorRegistry;

	public final Clazz bool, integer, object, string;
	public final Reference[] cuts, bools, objects, ints, strings;
	public final Constructorz cns, cns_bool, cns_int, cns_object, cns_string, cns_beh;
	public final Methodz getN, getThree, method0, method1, method2, method3, sMethod0, sMethod1, sMethod2, sMethod3;

	public final Constructorz obj_cns;

	public TestCoverageBehaviorCUT() throws Exception {
		super("test.coverage.Behavior");

		abstractorRegistry = new AbstractorRegistry(cluster, clusterBuilder.getXmlRegistry());

		assertEquals(5, cluster.getCluster().length);
		assertEquals("java.lang.Boolean", (bool = cluster.getCluster()[0]).toString());
		assertEquals("java.lang.Integer", (integer = cluster.getCluster()[1]).toString());
		assertEquals("java.lang.Object", (object = cluster.getCluster()[2]).toString());
		assertEquals("java.lang.String", (string = cluster.getCluster()[3]).toString());
		assertEquals("test.coverage.Behavior", cluster.getCluster()[4].toString());

		cuts    = refFactory.getReferences(cut);
		objects = refFactory.getReferences(object);
		bools = refFactory.getReferences(bool);
		ints    = refFactory.getReferences(integer);
		strings = refFactory.getReferences(string);

		assertEquals(1, object.getConstructors().length);
		assertEquals("java.lang.Object()", (obj_cns = object.getConstructors()[0]).toString());

		assertEquals(6, cut.getConstructors().length);
		assertEquals("test.coverage.Behavior()", (cns = cut.getConstructors()[0]).toString());
		assertEquals("test.coverage.Behavior(boolean)", (cns_bool = cut.getConstructors()[1]).toString());
		assertEquals("test.coverage.Behavior(int)", (cns_int = cut.getConstructors()[2]).toString());
		assertEquals("test.coverage.Behavior(java.lang.Object)", (cns_object = cut.getConstructors()[3]).toString());
		assertEquals("test.coverage.Behavior(java.lang.String)", (cns_string = cut.getConstructors()[4]).toString());
		assertEquals("test.coverage.Behavior(test.coverage.Behavior)", (cns_beh = cut.getConstructors()[5]).toString());

		assertEquals(10, cut.getMethods().length);
		assertEquals("getN()", (getN = cut.getMethods()[0]).toString());
		assertEquals("getThree()", (getThree = cut.getMethods()[1]).toString());
		assertEquals("method0(boolean)", (method0 = cut.getMethods()[2]).toString());
		assertEquals("method1(int, int)", (method1 = cut.getMethods()[3]).toString());
		assertEquals("method2(java.lang.Object, java.lang.Object)", (method2 = cut.getMethods()[4]).toString());
		assertEquals("method3(java.lang.String, java.lang.String)", (method3 = cut.getMethods()[5]).toString());
		assertEquals("sMethod0(boolean, boolean)", (sMethod0 = cut.getMethods()[6]).toString());
		assertEquals("sMethod1(int, int)", (sMethod1 = cut.getMethods()[7]).toString());
		assertEquals("sMethod2(java.lang.Object, java.lang.Object)", (sMethod2 = cut.getMethods()[8]).toString());
		assertEquals("sMethod3(java.lang.String, java.lang.String)", (sMethod3 = cut.getMethods()[9]).toString());

	}
}