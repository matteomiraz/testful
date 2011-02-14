/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
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

import junit.framework.TestCase;

/**
 * Checks the user runs the tests by using the adequate settings
 * @author matteo
 */
public class TestingSetupTestCase extends TestCase {

	public void testQuiet() throws Exception {
		assertTrue("It is strongly suggested to run the tests with the option \"-Dtestful.quiet=true\"", TestFul.quiet);
	}

}
