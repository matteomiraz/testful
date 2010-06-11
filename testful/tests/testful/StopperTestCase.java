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

package testful;

import testful.model.Constructorz;
import testful.model.Methodz;

/**
 * Stub for the test.coverage.Stopped class
 * @author matteo
 */
public abstract class StopperTestCase extends SingleClassTestCase {

	@Override
	protected String getClassUnderTest() {
		return "test.coverage.Stopped";
	}

	protected Constructorz cns;

	protected Methodz longMethod1;
	protected Methodz longMethod2;
	protected Methodz longMethod3;
	protected Methodz longMethod4;
	protected Methodz longMethod5;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		cns = cut.getConstructors()[0];

		longMethod1 = null;
		longMethod2 = null;
		longMethod3 = null;
		longMethod4 = null;
		longMethod5 = null;
		for(Methodz m : cut.getMethods()) {
			if(checkMethod(m, "longMethod1")) longMethod1 = m;
			if(checkMethod(m, "longMethod2")) longMethod2 = m;
			if(checkMethod(m, "longMethod3")) longMethod3 = m;
			if(checkMethod(m, "longMethod4")) longMethod4 = m;
			if(checkMethod(m, "longMethod5")) longMethod5 = m;
		}
		assertNotNull(longMethod1);
		assertNotNull(longMethod2);
		assertNotNull(longMethod3);
		assertNotNull(longMethod4);
		assertNotNull(longMethod5);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		cns = null;
		longMethod1 = null;
		longMethod2 = null;
		longMethod3 = null;
		longMethod4 = null;
		longMethod5 = null;
	}

}
