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

package testful.coverage.whiteBox;

/**
 * Exposes the internal state of a class, used to track its def-uses
 *
 * @author matteo
 */
public interface DefExposer {

	/**
	 * Returns the array with the latest defs of each field.
	 * For arrays, it returns the definition of the whole array (i.e., type[] f = new type[n]).
	 *
	 * Example: for a class with those fields: {i (int), I (Integer), is (int[]), IS (Integer[])} the method returns
	 * <code>
	 * return new Object[] {<br/>
	 *   __latest_def_of_i__, <br/>
	 *   __latest_def_of_I__, <br/>
	 *   __latest_def_of_is__, <i>// this is an array of DataAccess</i><br/>
	 *   __latest_def_of_IS__ <i>// this is an array of DataAccess</i><br/>
	 * };</code>
	 *
	 * @return the internal state
	 */
	Object[] __testful_get_defs__();

	public final String GET_DEFS = "__testful_get_defs__";

	/**
	 * Returns the array with the internal state of the object.
	 * This method returns only non-primitive type (for arrays, it checks the base type.)
	 *
	 * Example: for a class with those fields: {i (int), I (Integer), is (int[]), IS (Integer[])} the method returns
	 * <code>
	 * return new Object[] {<br/>
	 *   I, <br/>
	 *   IS, <br/>
	 * };</code>
	 *
	 * @return the internal state
	 */
	Object[] __testful_get_fields__();

	public final String GET_FIELDS = "__testful_get_fields__";

}
