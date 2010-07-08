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
 * Stub for the test.coverage.Fault class
 * @author matteo
 */
public class TestCoverageFaultCUT extends SingleTestCUT {

	public final Reference[] objects;
	public final Constructorz oCns;
	public final Constructorz cCns;

	public final Reference[] iterators;

	public final Methodz a, a1, a2;
	public final Methodz b, b1;
	public final Methodz c, c1, c2;
	public final Methodz d;
	public final Methodz e, e1;
	public final Methodz f, f1, f2, f3, f4;
	public final Methodz g, g1;

	public final Methodz i_hasNext, i_next, i_remove;

	public TestCoverageFaultCUT() throws Exception {
		super("test.coverage.Fault");

		final Clazz oClazz, iClazz;
		assertEquals(3, cluster.getCluster().length);
		assertEquals("java.lang.Object", (oClazz = cluster.getCluster()[0]).toString());
		assertEquals("java.util.Iterator", (iClazz = cluster.getCluster()[1]).toString());
		assertEquals("test.coverage.Fault", cluster.getCluster()[2].toString());

		assertEquals(1, cut.getConstructors().length);
		assertEquals("test.coverage.Fault()", (cCns = cut.getConstructors()[0]).toString());

		assertEquals(18, cut.getMethods().length);
		assertEquals("a(java.lang.Object)", (a = cut.getMethods()[0]).toString());
		assertEquals("a1(java.lang.Object)", (a1 = cut.getMethods()[1]).toString());
		assertEquals("a2(java.lang.Object)", (a2 = cut.getMethods()[2]).toString());
		assertEquals("b()", (b = cut.getMethods()[3]).toString());
		assertEquals("b1()", (b1 = cut.getMethods()[4]).toString());
		assertEquals("c()", (c = cut.getMethods()[5]).toString());
		assertEquals("c1()", (c1 = cut.getMethods()[6]).toString());
		assertEquals("c2()", (c2 = cut.getMethods()[7]).toString());
		assertEquals("d()", (d = cut.getMethods()[8]).toString());
		assertEquals("e()", (e = cut.getMethods()[9]).toString());
		assertEquals("e1()", (e1 = cut.getMethods()[10]).toString());
		assertEquals("f()", (f = cut.getMethods()[11]).toString());
		assertEquals("f1()", (f1 = cut.getMethods()[12]).toString());
		assertEquals("f2()", (f2 = cut.getMethods()[13]).toString());
		assertEquals("f3()", (f3 = cut.getMethods()[14]).toString());
		assertEquals("f4()", (f4 = cut.getMethods()[15]).toString());
		assertEquals("g()", (g = cut.getMethods()[16]).toString());
		assertEquals("g1()", (g1 = cut.getMethods()[17]).toString());

		objects = refFactory.getReferences(oClazz);
		iterators = refFactory.getReferences(iClazz);

		assertEquals(1, oClazz.getConstructors().length);
		assertEquals("java.lang.Object()", (oCns = oClazz.getConstructors()[0]).toString());

		assertEquals(3, iClazz.getMethods().length);
		assertEquals("hasNext()", (i_hasNext = iClazz.getMethods()[0]).toString());
		assertEquals("next()", (i_next = iClazz.getMethods()[1]).toString());
		assertEquals("remove()", (i_remove = iClazz.getMethods()[2]).toString());
	}
}