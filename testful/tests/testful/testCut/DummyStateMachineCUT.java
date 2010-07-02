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
public class DummyStateMachineCUT extends SingleTestCUT {
	public final Reference[] chars;

	public final Constructorz cns;

	public final Methodz getState;
	public final Methodz nextChar;
	public final Methodz nextChar_char;

	public DummyStateMachineCUT() throws Exception {
		super("dummy.StateMachine");

		Clazz cClazz = null;
		for(Clazz clazz : cluster.getCluster()) {
			if("java.lang.Character".equals(clazz.getClassName())) cClazz = clazz;
		}
		assertNotNull(cClazz);

		chars = refFactory.getReferences(cClazz);

		cns = cut.getConstructors()[0];

		Methodz _getState = null;
		Methodz _nextChar = null;
		Methodz _nextCharC = null;
		for(Methodz m : cut.getMethods()) {
			if(checkMethod(m, "getState")) _getState = m;
			if(checkMethod(m, "nextChar")) _nextChar = m;
			if(checkMethod(m, "nextChar", cClazz)) _nextCharC = m;
		}
		assertNotNull(_getState);
		assertNotNull(_nextChar);
		assertNotNull(_nextCharC);
		getState = _getState;
		nextChar = _nextChar;
		nextChar_char = _nextCharC;
	}
}
