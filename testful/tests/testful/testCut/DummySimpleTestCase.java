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

import testful.SingleClassTestCase;
import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.Methodz;
import testful.model.Reference;

public class DummySimpleTestCase extends SingleClassTestCase {

	/* (non-Javadoc)
	 * @see testful.SingleClassTestCase#getClassUnderTest()
	 */
	@Override
	protected String getClassUnderTest() {
		return "dummy.Simple";
	}

	protected Reference[] ints, objects;

	protected Constructorz oCns;

	protected Constructorz cns;

	protected Methodz mInc;
	protected Methodz mDec;
	protected Methodz oStatus;
	protected Methodz wModulo;
	protected Methodz oAbs;
	protected Methodz compare;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

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

		oCns = null;
		for (Constructorz c : oClazz.getConstructors()) {
			if(c.getParameterTypes().length == 0) oCns = c;
		}
		assertNotNull(oCns);

		cns = cut.getConstructors()[0];

		mInc = null;
		mDec = null;
		oStatus = null;
		wModulo = null;
		oAbs = null;
		compare = null;
		for(Methodz m : cut.getMethods()) {
			if("mInc".equals(m.getName())) mInc = m;
			if("mDec".equals(m.getName())) mDec = m;
			if("oStatus".equals(m.getName())) oStatus = m;
			if("wModulo".equals(m.getName())) wModulo = m;
			if("oAbs".equals(m.getName())) oAbs = m;
			if("compare".equals(m.getName())) compare = m;
		}
		assertNotNull(mInc);
		assertNotNull(mDec);
		assertNotNull(oStatus);
		assertNotNull(wModulo);
		assertNotNull(oAbs);
		assertNotNull(compare);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		ints = null;
		objects = null;

		oCns = null;

		cns      = null;

		mInc     = null;
		mDec     = null;
		oStatus  = null;
		wModulo  = null;
		oAbs     = null;
		compare  = null;
	}
}
