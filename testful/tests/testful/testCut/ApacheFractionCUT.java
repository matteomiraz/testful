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
import testful.model.StaticValue;

/**
 * Stub for the apache.Fraction class
 * @author matteo
 */
public class ApacheFractionCUT extends SingleTestCUT {
	public final Reference[] ints, doubles, objects;

	public final StaticValue TWO_THIRDS, THREE_QUARTERS, MINUS_ONE, ONE_FIFTH, THREE_FIFTHS, TWO, TWO_QUARTERS, ONE_HALF;

	public final Constructorz cns_int, cns_int_int, cns_double_int;

	public final Methodz hashCode, divide_Fraction,	equals, subtract_int, subtract_Fraction, multiply_Fraction, longValue, multiply_int, doubleValue;

	public ApacheFractionCUT() throws Exception {
		super("apache.Fraction");

		Clazz iClazz = null;
		Clazz dClazz = null;
		Clazz oClazz = null;
		for (Clazz clazz : cluster.getCluster()) {
			if ("java.lang.Integer".equals(clazz.getClassName())) iClazz = clazz;
			if ("java.lang.Double".equals(clazz.getClassName()))  dClazz = clazz;
			if ("java.lang.Object".equals(clazz.getClassName()))  oClazz = clazz;
		}
		assertNotNull(iClazz);
		assertNotNull(dClazz);
		assertNotNull(oClazz);

		ints = refFactory.getReferences(iClazz);
		doubles = refFactory.getReferences(dClazz);
		objects = refFactory.getReferences(oClazz);

		Constructorz _cns_int = null;
		Constructorz _cns_int_int = null;
		Constructorz _cns_double_int = null;
		for (Constructorz cns : cut.getConstructors()) {
			if (checkParameters(cns.getParameterTypes(), iClazz)) _cns_int = cns;
			if (checkParameters(cns.getParameterTypes(), iClazz, iClazz)) _cns_int_int = cns;
			if (checkParameters(cns.getParameterTypes(), dClazz, iClazz)) _cns_double_int = cns;
		}
		assertNotNull(_cns_int);
		assertNotNull(_cns_int_int);
		assertNotNull(_cns_double_int);
		cns_int = _cns_int;
		cns_int_int = _cns_int_int;
		cns_double_int = _cns_double_int;

		StaticValue _TWO_THIRDS = null;
		StaticValue _THREE_QUARTERS = null;
		StaticValue _MINUS_ONE = null;
		StaticValue _ONE_FIFTH = null;
		StaticValue _THREE_FIFTHS = null;
		StaticValue _TWO = null;
		StaticValue _TWO_QUARTERS = null;
		StaticValue _ONE_HALF = null;
		for (StaticValue c : cut.getConstants()) {
			if(c.getName().equals("TWO_THIRDS")) _TWO_THIRDS = c;
			if(c.getName().equals("THREE_QUARTERS")) _THREE_QUARTERS = c;
			if(c.getName().equals("MINUS_ONE")) _MINUS_ONE = c;
			if(c.getName().equals("ONE_FIFTH")) _ONE_FIFTH = c;
			if(c.getName().equals("THREE_FIFTHS")) _THREE_FIFTHS = c;
			if(c.getName().equals("TWO")) _TWO = c;
			if(c.getName().equals("TWO_QUARTERS")) _TWO_QUARTERS = c;
			if(c.getName().equals("ONE_HALF")) _ONE_HALF = c;
		}
		assertNotNull(_TWO_THIRDS);
		assertNotNull(_THREE_QUARTERS);
		assertNotNull(_MINUS_ONE);
		assertNotNull(_ONE_FIFTH);
		assertNotNull(_THREE_FIFTHS);
		assertNotNull(_TWO);
		assertNotNull(_TWO_QUARTERS);
		assertNotNull(_ONE_HALF);
		TWO_THIRDS = _TWO_THIRDS;
		THREE_QUARTERS = _THREE_QUARTERS;
		MINUS_ONE = _MINUS_ONE;
		ONE_FIFTH = _ONE_FIFTH;
		THREE_FIFTHS = _THREE_FIFTHS;
		TWO = _TWO;
		TWO_QUARTERS = _TWO_QUARTERS;
		ONE_HALF = _ONE_HALF;

		Methodz _divide_Fraction = null;
		Methodz _hashCode = null;
		Methodz _equals = null;
		Methodz _subtract_int = null;
		Methodz _subtract_Fraction = null;
		Methodz _multiply_Fraction = null;
		Methodz _longValue = null;
		Methodz _multiply_int = null;
		Methodz _doubleValue = null;
		for (Methodz m : cut.getMethods()) {
			if (checkMethod(m, "divide", cut)) _divide_Fraction = m;
			if (checkMethod(m, "hashCode")) _hashCode = m;
			if (checkMethod(m, "equals", oClazz)) _equals = m;
			if (checkMethod(m, "subtract", iClazz)) _subtract_int = m;
			if (checkMethod(m, "subtract", cut)) _subtract_Fraction = m;
			if (checkMethod(m, "multiply", cut)) _multiply_Fraction = m;
			if (checkMethod(m, "longValue")) _longValue = m;
			if (checkMethod(m, "doubleValue")) _doubleValue = m;
			if (checkMethod(m, "multiply", iClazz)) _multiply_int = m;
		}
		assertNotNull(_divide_Fraction);
		assertNotNull(_hashCode);
		assertNotNull(_equals);
		assertNotNull(_subtract_int);
		assertNotNull(_subtract_Fraction);
		assertNotNull(_multiply_Fraction);
		assertNotNull(_longValue);
		assertNotNull(_doubleValue);
		assertNotNull(_multiply_int);
		divide_Fraction = _divide_Fraction;
		hashCode = _hashCode;
		equals = _equals;
		subtract_int = _subtract_int;
		subtract_Fraction = _subtract_Fraction;
		multiply_Fraction = _multiply_Fraction;
		longValue = _longValue;
		doubleValue = _doubleValue;
		multiply_int = _multiply_int;
	}
}
