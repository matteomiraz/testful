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
public class TestCoverageControlFlowCUT extends SingleTestCUT {

	public final Reference[] cuts, bools, doubles, ints;

	public final Constructorz cns;

	public final Methodz boolif, doubleif, intif, lSwitch, objif, tSwitch;

	public TestCoverageControlFlowCUT() throws Exception {
		super("test.coverage.ControlFlow");

		Clazz bType, dType, iType;
		assertEquals(4, cluster.getCluster().length);
		assertEquals("java.lang.Boolean", (bType = cluster.getCluster()[0]).toString());
		assertEquals("java.lang.Double",  (dType = cluster.getCluster()[1]).toString());
		assertEquals("java.lang.Integer", (iType = cluster.getCluster()[2]).toString());
		assertEquals("test.coverage.ControlFlow", cluster.getCluster()[3].toString());

		cuts    = refFactory.getReferences(cut);
		bools   = refFactory.getReferences(bType);
		doubles = refFactory.getReferences(dType);
		ints    = refFactory.getReferences(iType);

		assertEquals(1, cut.getConstructors().length);
		assertEquals("test.coverage.ControlFlow()", (cns = cut.getConstructors()[0]).toString());

		assertEquals(6, cut.getMethods().length);
		assertEquals("boolif(boolean)", (boolif = cut.getMethods()[0]).toString());
		assertEquals("doubleif(double)", (doubleif = cut.getMethods()[1]).toString());
		assertEquals("intif(int)", (intif = cut.getMethods()[2]).toString());
		assertEquals("lSwitch(int)", (lSwitch = cut.getMethods()[3]).toString());
		assertEquals("objif(test.coverage.ControlFlow)", (objif = cut.getMethods()[4]).toString());
		assertEquals("tSwitch(int)", (tSwitch = cut.getMethods()[5]).toString());
	}
}