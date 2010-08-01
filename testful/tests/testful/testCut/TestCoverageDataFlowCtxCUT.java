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
public class TestCoverageDataFlowCtxCUT extends SingleTestCUT {

	public final Reference[] cuts, bools, ints, inners;

	public final Constructorz cut_cns;

	public final Methodz getA, getI, getIA, getIB, setA, setI, setIA, setIB, usePropagation;

	public final Methodz i_getIA, i_getIB, i_setIA, i_setIB;

	public TestCoverageDataFlowCtxCUT() throws Exception {
		super("test.coverage.DataFlowCtx");

		final Clazz bType, iType, IType;
		assertEquals(4, cluster.getCluster().length);
		assertEquals("java.lang.Boolean", (bType = cluster.getCluster()[0]).toString());
		assertEquals("java.lang.Integer", (iType = cluster.getCluster()[1]).toString());
		assertEquals("test.coverage.DataFlowCtx", cluster.getCluster()[2].toString());
		assertEquals("test.coverage.DataFlowCtx$Inner", (IType = cluster.getCluster()[3]).toString());

		bools = refFactory.getReferences(bType);
		ints = refFactory.getReferences(iType);
		inners = refFactory.getReferences(IType);
		cuts = refFactory.getReferences(cut);

		assertEquals(1, cut.getConstructors().length);
		assertEquals("test.coverage.DataFlowCtx()", (cut_cns = cut.getConstructors()[0]).toString());

		assertEquals(9, cut.getMethods().length);
		assertEquals("getA()", (getA = cut.getMethods()[0]).toString());
		assertEquals("getI()", (getI = cut.getMethods()[1]).toString());
		assertEquals("getIA()", (getIA = cut.getMethods()[2]).toString());
		assertEquals("getIB()", (getIB = cut.getMethods()[3]).toString());
		assertEquals("setA(int)", (setA = cut.getMethods()[4]).toString());
		assertEquals("setI(test.coverage.DataFlowCtx$Inner)", (setI = cut.getMethods()[5]).toString());
		assertEquals("setIA(int)", (setIA = cut.getMethods()[6]).toString());
		assertEquals("setIB(int)", (setIB = cut.getMethods()[7]).toString());
		assertEquals("usePropagation(boolean)", (usePropagation = cut.getMethods()[8]).toString());

		assertEquals(4, IType.getMethods().length);
		assertEquals("getIA()", (i_getIA = IType.getMethods()[0]).toString());
		assertEquals("getIB()", (i_getIB = IType.getMethods()[1]).toString());
		assertEquals("setIA(int)", (i_setIA = IType.getMethods()[2]).toString());
		assertEquals("setIB(int)", (i_setIB = IType.getMethods()[3]).toString());
	}
}