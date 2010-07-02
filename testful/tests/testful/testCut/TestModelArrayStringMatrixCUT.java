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
public class TestModelArrayStringMatrixCUT extends SingleTestCUT {
	public final Reference[] ints, strings, sa, saa;

	public final Constructorz c_cns;
	public final Methodz c_crea;
	public final Methodz c_conta;

	public final Constructorz sa_cns;
	public final Methodz sa_addHead, sa_getTail;

	public final Constructorz saa_cns;
	public final Methodz saa_addTail;

	public TestModelArrayStringMatrixCUT() throws Exception {
		super("test.model.array.StringMatrix");

		Clazz integer = null;
		Clazz string = null;
		Clazz stringArray = null;
		Clazz stringArrayArray = null;
		for(Clazz clazz : cluster.getCluster()) {
			if("java.lang.Integer".equals(clazz.getClassName())) integer = clazz;
			if("java.lang.String".equals(clazz.getClassName())) string = clazz;
			if("test.model.array.StringArray".equals(clazz.getClassName())) stringArray = clazz;
			if("test.model.array.StringArrayArray".equals(clazz.getClassName())) stringArrayArray = clazz;
		}
		assertNotNull(integer);
		assertNotNull(string);
		assertNotNull(stringArray);
		assertNotNull(stringArrayArray);

		c_cns = cut.getConstructors()[0];

		Methodz _testful_crea = null;
		Methodz _testful_conta = null;
		for(Methodz m : cut.getMethods()) {
			if("testful_crea".equals(m.getName()))  _testful_crea = m;
			if("testful_conta".equals(m.getName())) _testful_conta = m;
		}
		assertNotNull(_testful_crea);
		assertNotNull(_testful_conta);

		c_crea  = _testful_crea;
		c_conta = _testful_conta;

		ints = refFactory.getReferences(integer);
		strings = refFactory.getReferences(string);

		// -------------- stringArray ----------------------
		sa = refFactory.getReferences(stringArray);

		Constructorz _saCns = null;
		for (Constructorz c : stringArray.getConstructors()) {
			if(c.getParameterTypes().length == 0) _saCns = c;
		}
		assertNotNull(_saCns);
		sa_cns = _saCns;

		Methodz _sa_addHead = null;
		Methodz _sa_getTail = null;
		for (Methodz m : stringArray.getMethods()) {
			if(checkMethod(m, "addHead", string)) _sa_addHead = m;
			if(checkMethod(m, "getTail")) _sa_getTail = m;
		}
		assertNotNull(_sa_addHead);
		assertNotNull(_sa_getTail);
		sa_addHead = _sa_addHead;
		sa_getTail = _sa_getTail;

		// -------------- stringArrayArray ----------------------
		saa = refFactory.getReferences(stringArrayArray);

		Constructorz _saaCns = null;
		for (Constructorz c : stringArrayArray.getConstructors()) {
			if(c.getParameterTypes().length == 0) _saaCns = c;
		}
		assertNotNull(_saaCns);
		saa_cns = _saaCns;

		Methodz _saa_addTail = null;
		for (Methodz m : stringArrayArray.getMethods()) {
			if(checkMethod(m, "addTail", stringArray)) _saa_addTail = m;
		}
		assertNotNull(_saa_addTail);
		saa_addTail = _saa_addTail;


	}
}
