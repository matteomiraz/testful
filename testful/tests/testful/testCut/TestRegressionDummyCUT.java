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

public class TestRegressionDummyCUT extends SingleTestCUT {

	public final Clazz boolClazz, byteClazz, charClazz, doubleClazz, floatClazz, intClazz, longClazz, shortClazz, stringClazz;
	public final Reference[] cuts, bools, bytes, chars, doubles, floats, ints, longs, shorts, strings;

	public final Constructorz cns;

	public final Methodz getByte, getChar, getDouble, getDummy, getFloat, getInt, getLong, getShort, getString, isBoolean;
	public final Methodz setBoolean, setByte, setChar, setDouble, setDummy, setFloat, setInt, setLong, setShort, setString;

	public TestRegressionDummyCUT() throws Exception {
		super("test.regression.Dummy");

		// cluster.getCluster()
		assertEquals(10, cluster.getCluster().length);
		assertEquals("java.lang.Boolean", (boolClazz = cluster.getCluster()[0]).toString());
		assertEquals("java.lang.Byte", (byteClazz = cluster.getCluster()[1]).toString());
		assertEquals("java.lang.Character", (charClazz = cluster.getCluster()[2]).toString());
		assertEquals("java.lang.Double", (doubleClazz = cluster.getCluster()[3]).toString());
		assertEquals("java.lang.Float", (floatClazz = cluster.getCluster()[4]).toString());
		assertEquals("java.lang.Integer", (intClazz = cluster.getCluster()[5]).toString());
		assertEquals("java.lang.Long", (longClazz = cluster.getCluster()[6]).toString());
		assertEquals("java.lang.Short", (shortClazz = cluster.getCluster()[7]).toString());
		assertEquals("java.lang.String", (stringClazz = cluster.getCluster()[8]).toString());
		assertEquals("test.regression.Dummy", cluster.getCluster()[9].toString());

		cuts = refFactory.getReferences(cut);
		bools = refFactory.getReferences(boolClazz);
		bytes = refFactory.getReferences(byteClazz);
		chars = refFactory.getReferences(charClazz);
		doubles = refFactory.getReferences(doubleClazz);
		floats = refFactory.getReferences(floatClazz);
		ints = refFactory.getReferences(intClazz);
		longs = refFactory.getReferences(longClazz);
		shorts = refFactory.getReferences(shortClazz);
		strings = refFactory.getReferences(stringClazz);

		// cut.getConstructors()
		assertEquals(1, cut.getConstructors().length);
		assertEquals("test.regression.Dummy()", (cns = cut.getConstructors()[0]).toString());

		// cut.getMethods()
		assertEquals(20, cut.getMethods().length);
		assertEquals("getByte()", (getByte = cut.getMethods()[0]).toString());
		assertEquals("getChar()", (getChar = cut.getMethods()[1]).toString());
		assertEquals("getDouble()", (getDouble = cut.getMethods()[2]).toString());
		assertEquals("getDummy()", (getDummy = cut.getMethods()[3]).toString());
		assertEquals("getFloat()", (getFloat = cut.getMethods()[4]).toString());
		assertEquals("getInt()", (getInt = cut.getMethods()[5]).toString());
		assertEquals("getLong()", (getLong = cut.getMethods()[6]).toString());
		assertEquals("getShort()", (getShort = cut.getMethods()[7]).toString());
		assertEquals("getString()", (getString = cut.getMethods()[8]).toString());
		assertEquals("isBoolean()", (isBoolean = cut.getMethods()[9]).toString());
		assertEquals("setBoolean(boolean)", (setBoolean = cut.getMethods()[10]).toString());
		assertEquals("setByte(byte)", (setByte = cut.getMethods()[11]).toString());
		assertEquals("setChar(char)", (setChar = cut.getMethods()[12]).toString());
		assertEquals("setDouble(double)", (setDouble = cut.getMethods()[13]).toString());
		assertEquals("setDummy(test.regression.Dummy)", (setDummy = cut.getMethods()[14]).toString());
		assertEquals("setFloat(float)", (setFloat = cut.getMethods()[15]).toString());
		assertEquals("setInt(int)", (setInt = cut.getMethods()[16]).toString());
		assertEquals("setLong(long)", (setLong = cut.getMethods()[17]).toString());
		assertEquals("setShort(short)", (setShort = cut.getMethods()[18]).toString());
		assertEquals("setString(java.lang.String)", (setString = cut.getMethods()[19]).toString());
	}
}