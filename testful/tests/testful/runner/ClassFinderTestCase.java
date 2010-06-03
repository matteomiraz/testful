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

package testful.runner;

import java.util.List;

import junit.framework.TestCase;
import testful.GenericTestCase;

public class ClassFinderTestCase extends TestCase {

	public void testInnerClasses() throws Exception {
		ClassFinder finder =
			new ClassFinderCaching(
					new ClassFinderImpl(GenericTestCase.config.getDirInstrumented(), GenericTestCase.config.getDirContracts(), GenericTestCase.config.getDirCompiled())
			);
		List<String> innerClasses = finder.getInnerClasses("test.instrument.Nested");

		String[] expected = {
				"test.instrument.Nested$1",
				"test.instrument.Nested$Inner",
				"test.instrument.Nested$Inner$Inner3",
				"test.instrument.Nested$InnerStatic",
				"test.instrument.Nested$InnerStatic$Inner2",
				"test.instrument.Nested$InnerStatic$InnerStatic2",
				"test.instrument.Nested$PrivateStatic",
		};

		assertEquals(expected.length, innerClasses.size());
		for (String exp : expected) {
			assertTrue(innerClasses.contains(exp));
			try {
				byte[] expBytecode = finder.getClass(exp);
				assertNotNull(expBytecode);
				assertTrue(expBytecode.length > 0);
			} catch (ClassNotFoundException e) {
				fail("Cannot load the inner class " + exp);
			}
		}
	}

	public void testDefaultInnerClasses() throws Exception {
		ClassFinder finder =
			new ClassFinderCaching(
					new ClassFinderImpl(GenericTestCase.config.getDirInstrumented(), GenericTestCase.config.getDirContracts(), GenericTestCase.config.getDirCompiled())
			);
		List<String> innerClasses = finder.getInnerClasses("Nested");

		String[] expected = {
				"Nested$1",
				"Nested$Inner",
				"Nested$Inner$Inner3",
				"Nested$InnerStatic",
				"Nested$InnerStatic$Inner2",
				"Nested$InnerStatic$InnerStatic2",
				"Nested$PrivateStatic",
		};

		assertEquals(expected.length, innerClasses.size());
		for (String exp : expected) {
			assertTrue("Inner class " + exp + " not found", innerClasses.contains(exp));
			try {
				byte[] expBytecode = finder.getClass(exp);
				assertNotNull(expBytecode);
				assertTrue(expBytecode.length > 0);
			} catch (ClassNotFoundException e) {
				fail("Cannot load the inner class " + exp);
			}
		}
	}

	public void testNoInnerClasse() throws Exception {
		ClassFinder finder =
			new ClassFinderCaching(
					new ClassFinderImpl(GenericTestCase.config.getDirInstrumented(), GenericTestCase.config.getDirContracts(), GenericTestCase.config.getDirCompiled())
			);
		List<String> innerClasses = finder.getInnerClasses("apache.Fraction");

		assertTrue(innerClasses.isEmpty());
	}

}
