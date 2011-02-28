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

		assertEquals(2, cluster.getClusterSize());

		final Clazz cut = cluster.getCut();
		final Clazz iClass = cluster.getCluster(0);
		assertEquals("java.lang.Integer", iClass.getClassName());

		assertEquals(1, cut.getConstructors().length);
		assertEquals(0, cut.getConstructors()[0].getParameterTypes().length);

		assertEquals(2, cut.getMethods().length);
		assertEquals("m2", cut.getMethods()[0].getName());
		assertEquals("m5", cut.getMethods()[1].getName());

		assertEquals(1, cut.getConstants().length);

		assertEquals(4, iClass.getConstants().length);
		assertEquals("test.model.Array.i", iClass.getConstants()[3].toString());
	}

}
