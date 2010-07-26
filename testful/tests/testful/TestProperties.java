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

/**
 * Utility class to hold properties for testing testful.
 * @author matteo
 */
public class TestProperties {

	/** Run random tests and check for errors in TestFul (boolean) */
	public static final String PROPERTY_TEST_RANDOM = "test.random";

	/** Number of iterations of random test (integer) */
	public static final String PROPERTY_TEST_RANDOM_ITERATIONS = "test.random.iterations";

	/** Run an extensive random test (boolean) */
	public static final String PROPERTY_TEST_RANDOM_EXTENSIVE = "test.random.extensive";

}
