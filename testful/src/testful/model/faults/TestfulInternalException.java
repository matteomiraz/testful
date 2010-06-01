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

package testful.model.faults;

import java.io.Serializable;

/**
 * Marker for internal exceptions, which denote internal problems of testful.
 * @author matteo
 */
public interface TestfulInternalException extends Serializable {

	/**
	 * Default implementation for Testful Internal Exception.
	 * @author matteo
	 */
	public static class Impl extends RuntimeException implements TestfulInternalException {

		private static final long serialVersionUID = -6508965666007161067L;

		public Impl(Throwable e) {
			super(null, e);
		}

		public Impl(String msg, Throwable e) {
			super(msg, e);
		}
	}
}
