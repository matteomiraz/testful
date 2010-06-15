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

package testful.testCut;

import static junit.framework.Assert.assertNotNull;
import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.Methodz;
import testful.model.Reference;

/**
 * Stub for the dummy.Simple class
 * @author matteo
 */
public class DummySimpleCUT extends SingleTestCUT {
	public final Reference[] ints, objects;

	public final Constructorz oCns;

	public final Constructorz cns;

	public final Methodz mInc;
	public final Methodz mDec;
	public final Methodz oStatus;
	public final Methodz wModulo;
	public final Methodz oAbs;
	public final Methodz compare;

	public DummySimpleCUT() throws Exception {
		super("dummy.Simple");

		Clazz iClazz = null;
		Clazz oClazz = null;
		for(Clazz clazz : cluster.getCluster()) {
			if("java.lang.Integer".equals(clazz.getClassName())) iClazz = clazz;
			if("java.lang.Object".equals(clazz.getClassName())) oClazz = clazz;
		}
		assertNotNull(iClazz);
		assertNotNull(oClazz);

		ints = refFactory.getReferences(iClazz);
		objects = refFactory.getReferences(oClazz);

		Constructorz _oCns = null;
		for (Constructorz c : oClazz.getConstructors()) {
			if(c.getParameterTypes().length == 0) _oCns = c;
		}
		assertNotNull(_oCns);
		oCns = _oCns;

		cns = cut.getConstructors()[0];

		Methodz _mInc = null;
		Methodz _mDec = null;
		Methodz _oStatus = null;
		Methodz _wModulo = null;
		Methodz _oAbs = null;
		Methodz _compare = null;
		for(Methodz m : cut.getMethods()) {
			if("mInc".equals(m.getName())) _mInc = m;
			if("mDec".equals(m.getName())) _mDec = m;
			if("oStatus".equals(m.getName())) _oStatus = m;
			if("wModulo".equals(m.getName())) _wModulo = m;
			if("oAbs".equals(m.getName())) _oAbs = m;
			if("compare".equals(m.getName())) _compare = m;
		}
		assertNotNull(_mInc);
		assertNotNull(_mDec);
		assertNotNull(_oStatus);
		assertNotNull(_wModulo);
		assertNotNull(_oAbs);
		assertNotNull(_compare);

		mInc    = _mInc    ;
		mDec    = _mDec    ;
		oStatus = _oStatus ;
		wModulo = _wModulo ;
		oAbs    = _oAbs    ;
		compare = _compare ;
	}
}
