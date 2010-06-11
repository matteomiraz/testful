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

package testful;

import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.Methodz;
import testful.model.Reference;

/**
 * Stub for the apache.Fraction class
 * @author matteo
 */
public class ApacheFractionTestCase extends SingleClassTestCase {
	protected Reference[] ints, doubles;

	protected Constructorz cns_double_int;

	protected Methodz divide_Fraction;

	/* (non-Javadoc)
	 * @see testful.SingleClassTestCase#getClassUnderTest()
	 */
	@Override
	protected String getClassUnderTest() {
		return "apache.Fraction";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Clazz iClazz = null;
		Clazz dClazz = null;
		Clazz[] cl = cluster.getCluster();
		for(Clazz clazz : cl) {
			if("java.lang.Integer".equals(clazz.getClassName())) iClazz = clazz;
			if("java.lang.Double".equals(clazz.getClassName())) dClazz = clazz;
		}
		assertNotNull(iClazz);
		assertNotNull(dClazz);

		ints = refFactory.getReferences(iClazz);
		doubles = refFactory.getReferences(dClazz);

		cns_double_int = null;
		for (Constructorz cns : cut.getConstructors()) {
			if(checkParameters(cns.getParameterTypes(), dClazz, iClazz)) cns_double_int = cns;
		}
		assertNotNull(cns_double_int);

		divide_Fraction = null;
		for(Methodz m : cut.getMethods()) {
			if(checkMethod(m, "divide", cut)) divide_Fraction = m;
		}
		assertNotNull(divide_Fraction);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		cluster = null;
		refFactory = null;

		cuts = null;
		ints = null;
		doubles = null;

		cns_double_int = null;

		divide_Fraction = null;
	}
}
