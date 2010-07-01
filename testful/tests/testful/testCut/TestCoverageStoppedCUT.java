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
 * Stub for the test.coverage.Stopped class
 * @author matteo
 */
public class TestCoverageStoppedCUT extends SingleTestCUT {

	public final Reference[] ints;

	public final Constructorz cns;

	public final Methodz execute;
	public final Methodz longMethod1;
	public final Methodz longMethod2;
	public final Methodz longMethod3;
	public final Methodz longMethod4;
	public final Methodz longMethod5;
	public final Methodz infLoop;

	public TestCoverageStoppedCUT() throws Exception {
		super("test.coverage.Stopped");

		cns = cut.getConstructors()[0];

		Clazz iClazz = null;
		for(Clazz clazz : cluster.getCluster()) {
			if("java.lang.Integer".equals(clazz.getClassName())) iClazz = clazz;
		}
		assertNotNull(iClazz);
		ints = refFactory.getReferences(iClazz);

		Methodz _execute = null;
		Methodz _longMethod1 = null;
		Methodz _longMethod2 = null;
		Methodz _longMethod3 = null;
		Methodz _longMethod4 = null;
		Methodz _longMethod5 = null;
		Methodz _infLoop = null;
		for(Methodz m : cut.getMethods()) {
			if(checkMethod(m, "execute"))     _execute = m;
			if(checkMethod(m, "longMethod1")) _longMethod1 = m;
			if(checkMethod(m, "longMethod2")) _longMethod2 = m;
			if(checkMethod(m, "longMethod3")) _longMethod3 = m;
			if(checkMethod(m, "longMethod4")) _longMethod4 = m;
			if(checkMethod(m, "longMethod5")) _longMethod5 = m;
			if(checkMethod(m, "infLoop", iClazz, iClazz)) _infLoop = m;
		}
		assertNotNull(_execute);
		assertNotNull(_longMethod1);
		assertNotNull(_longMethod2);
		assertNotNull(_longMethod3);
		assertNotNull(_longMethod4);
		assertNotNull(_longMethod5);
		assertNotNull(_infLoop);
		execute     = _execute    ;
		longMethod1 = _longMethod1;
		longMethod2 = _longMethod2;
		longMethod3 = _longMethod3;
		longMethod4 = _longMethod4;
		longMethod5 = _longMethod5;
		infLoop     = _infLoop    ;
	}
}
