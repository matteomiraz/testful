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
import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.Methodz;
import testful.model.Reference;

/**
 * Stub for the test.coverage.DataFlow class
 * @author matteo
 */
public class TestCoveragePUseCUT extends SingleTestCUT {

	public final Reference[] cuts, ints;

	public final Constructorz cut_cns;

	public final Methodz ifFieldGT10, setA, zero;

	public TestCoveragePUseCUT() throws Exception {
		super("test.coverage.PUse");

		Clazz iType;
		assertEquals(2, cluster.getCluster().length);
		assertEquals("java.lang.Integer", (iType = cluster.getCluster()[0]).toString());
		assertEquals("test.coverage.PUse", cluster.getCluster()[1].toString());

		cuts = refFactory.getReferences(cut);
		ints = refFactory.getReferences(iType);

		assertEquals(1, cut.getConstructors().length);
		assertEquals("test.coverage.PUse()", (cut_cns = cut.getConstructors()[0]).toString());

		assertEquals(3, cut.getMethods().length);
		assertEquals("ifFieldGT10()", (ifFieldGT10 = cut.getMethods()[0]).toString());
		assertEquals("setA(int)", (setA = cut.getMethods()[1]).toString());
		assertEquals("zero()", (zero = cut.getMethods()[2]).toString());

	}
}