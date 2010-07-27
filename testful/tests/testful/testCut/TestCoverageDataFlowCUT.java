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
public class TestCoverageDataFlowCUT extends SingleTestCUT {

	public final Clazz bType, iType;

	public final Reference[] cuts, bools, ints;

	public final Constructorz cut_cns;

	public final Methodz getA, getIA, getIB, prova, setA, setIA, setIB;

	public TestCoverageDataFlowCUT() throws Exception {
		super("test.coverage.DataFlow");

		assertEquals(3, cluster.getCluster().length);
		assertEquals("java.lang.Boolean", (bType = cluster.getCluster()[0]).toString());
		assertEquals("java.lang.Integer", (iType = cluster.getCluster()[1]).toString());
		assertEquals("test.coverage.DataFlow", cluster.getCluster()[2].toString());

		bools = refFactory.getReferences(bType);
		ints = refFactory.getReferences(iType);
		cuts = refFactory.getReferences(cut);

		assertEquals(1, cut.getConstructors().length);
		assertEquals("test.coverage.DataFlow()", (cut_cns = cut.getConstructors()[0]).toString());

		assertEquals(7, cut.getMethods().length);
		assertEquals("getA()", (getA = cut.getMethods()[0]).toString());
		assertEquals("getIA()", (getIA = cut.getMethods()[1]).toString());
		assertEquals("getIB()", (getIB = cut.getMethods()[2]).toString());
		assertEquals("prova(boolean)", (prova = cut.getMethods()[3]).toString());
		assertEquals("setA(int)", (setA = cut.getMethods()[4]).toString());
		assertEquals("setIA(int)", (setIA = cut.getMethods()[5]).toString());
		assertEquals("setIB(int)", (setIB = cut.getMethods()[6]).toString());
	}

}