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

import static junit.framework.Assert.assertNotNull;
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

	public final Methodz a, a1, a2;
	public final Methodz b, b1;
	public final Methodz c, c1, c2;
	public final Methodz d;
	public final Methodz e, e1;
	public final Methodz f, f1, f2, f3, f4;

	public TestCoverageFaultCUT() throws Exception {
		super("test.coverage.Fault");
		Clazz oClazz = null;
		for(Clazz clazz : cluster.getCluster()) {
			if("java.lang.Object".equals(clazz.getClassName())) oClazz = clazz;
		}
		assertNotNull(oClazz);

		objects = refFactory.getReferences(oClazz);

		cCns = cut.getConstructors()[0];
		assertNotNull(cCns);

		Constructorz _oCns = null;
		for(Constructorz c : cut.getConstructors()) {
			if(checkParameters(c.getParameterTypes(), new Clazz[] { })) _oCns = c;
		}
		assertNotNull(_oCns);
		oCns = _oCns;

		Methodz _a = null;
		Methodz _a1 = null;
		Methodz _a2 = null;
		Methodz _b = null;
		Methodz _b1 = null;
		Methodz _c = null;
		Methodz _c1 = null;
		Methodz _c2 = null;
		Methodz _d = null;
		Methodz _e = null;
		Methodz _e1 = null;
		Methodz _f = null;
		Methodz _f1 = null;
		Methodz _f2 = null;
		Methodz _f3 = null;
		Methodz _f4 = null;
		for(Methodz m : cut.getMethods()) {
			if(checkMethod(m, "a" , oClazz)) _a  = m;
			if(checkMethod(m, "a1", oClazz)) _a1 = m;
			if(checkMethod(m, "a2", oClazz)) _a2 = m;
			if(checkMethod(m, "b"         )) _b  = m;
			if(checkMethod(m, "b1"        )) _b1 = m;
			if(checkMethod(m, "c"         )) _c  = m;
			if(checkMethod(m, "c1"        )) _c1 = m;
			if(checkMethod(m, "c2"        )) _c2 = m;
			if(checkMethod(m, "d"         )) _d  = m;
			if(checkMethod(m, "e"         )) _e  = m;
			if(checkMethod(m, "e1"        )) _e1 = m;
			if(checkMethod(m, "f"         )) _f  = m;
			if(checkMethod(m, "f1"        )) _f1 = m;
			if(checkMethod(m, "f2"        )) _f2 = m;
			if(checkMethod(m, "f3"        )) _f3 = m;
			if(checkMethod(m, "f4"        )) _f4 = m;
		}
		assertNotNull(_a);
		assertNotNull(_a1);
		assertNotNull(_a2);
		assertNotNull(_b );
		assertNotNull(_b1);
		assertNotNull(_c );
		assertNotNull(_c1);
		assertNotNull(_c2);
		assertNotNull(_d );
		assertNotNull(_e );
		assertNotNull(_e1);
		assertNotNull(_f );
		assertNotNull(_f1);
		assertNotNull(_f2);
		assertNotNull(_f3);
		assertNotNull(_f4);

		a  = _a  ;
		a1 = _a1 ;
		a2 = _a2 ;
		b  = _b  ;		b1 = _b1 ;		c  = _c  ;		c1 = _c1 ;		c2 = _c2 ;		d  = _d  ;		e  = _e  ;
		e1 = _e1 ;
		f  = _f  ;
		f1 = _f1 ;
		f2 = _f2 ;
		f3 = _f3 ;
		f4 = _f4 ;
	}
}