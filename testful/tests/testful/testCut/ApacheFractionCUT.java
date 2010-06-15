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
 * Stub for the apache.Fraction class
 * @author matteo
 */
public class ApacheFractionCUT extends SingleTestCUT {
	public final Reference[] ints, doubles;

	public final Constructorz cns_double_int;

	public final Methodz divide_Fraction;

	public ApacheFractionCUT() throws Exception {
		super("apache.Fraction");

		Clazz iClazz = null;
		Clazz dClazz = null;
		for (Clazz clazz : cluster.getCluster()) {
			if ("java.lang.Integer".equals(clazz.getClassName()))
				iClazz = clazz;
			if ("java.lang.Double".equals(clazz.getClassName()))
				dClazz = clazz;
		}
		assertNotNull(iClazz);
		assertNotNull(dClazz);

		ints = refFactory.getReferences(iClazz);
		doubles = refFactory.getReferences(dClazz);

		Constructorz _cns_double_int = null;
		for (Constructorz cns : cut.getConstructors()) {
			if (checkParameters(cns.getParameterTypes(), dClazz, iClazz))
				_cns_double_int = cns;
		}
		assertNotNull(_cns_double_int);
		cns_double_int = _cns_double_int;

		Methodz _divide_Fraction = null;
		for (Methodz m : cut.getMethods()) {
			if (checkMethod(m, "divide", cut))
				_divide_Fraction = m;
		}
		assertNotNull(_divide_Fraction);
		divide_Fraction = _divide_Fraction;
	}
}
