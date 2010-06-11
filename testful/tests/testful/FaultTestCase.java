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

package testful;

import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.Methodz;
import testful.model.Reference;

/**
 * Stub for the test.coverage.Fault class
 * @author matteo
 */
public abstract class FaultTestCase extends SingleClassTestCase {

	@Override
	protected String getClassUnderTest() {
		return "test.coverage.Fault";
	}

	protected Reference[] objects;
	protected Constructorz oCns;
	protected Constructorz cCns;

	protected Methodz a, a1, a2;
	protected Methodz b, b1;
	protected Methodz c, c1, c2;
	protected Methodz d;
	protected Methodz e, e1;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Clazz oClazz = null;
		for(Clazz clazz : cluster.getCluster()) {
			if("java.lang.Object".equals(clazz.getClassName())) oClazz = clazz;
		}
		assertNotNull(oClazz);

		objects = refFactory.getReferences(oClazz);

		cCns = cut.getConstructors()[0];
		assertNotNull(cCns);

		oCns = null;
		for(Constructorz c : cut.getConstructors()) {
			if(checkParameters(c.getParameterTypes(), new Clazz[] { })) oCns = c;
		}
		assertNotNull(oCns);

		a = null;
		a1 = null;
		a2 = null;
		b = null;
		b1 = null;
		c = null;
		c1 = null;
		c2 = null;
		d = null;
		e = null;
		e1 = null;
		for(Methodz m : cut.getMethods()) {
			if(checkMethod(m, "a" , oClazz)) a  = m;
			if(checkMethod(m, "a1", oClazz)) a1 = m;
			if(checkMethod(m, "a2", oClazz)) a2 = m;
			if(checkMethod(m, "b"         )) b  = m;
			if(checkMethod(m, "b1"        )) b1 = m;
			if(checkMethod(m, "c"         )) c  = m;
			if(checkMethod(m, "c1"        )) c1 = m;
			if(checkMethod(m, "c2"        )) c2 = m;
			if(checkMethod(m, "d"         )) d  = m;
			if(checkMethod(m, "e"         )) e  = m;
			if(checkMethod(m, "e1"        )) e1 = m;
		}
		assertNotNull(a);
		assertNotNull(a1);
		assertNotNull(a2);
		assertNotNull(b );
		assertNotNull(b1);
		assertNotNull(c );
		assertNotNull(c1);
		assertNotNull(c2);
		assertNotNull(d );
		assertNotNull(e );
		assertNotNull(e1);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		objects = null;
		cCns = null;
		oCns = null;

		a = null;
		a1 = null;
		a2 = null;
		b = null;
		b1 = null;
		c = null;
		c1 = null;
		c2 = null;
		d = null;
		e = null;
		e1 = null;
	}
}
