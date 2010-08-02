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

package testful.coverage.whiteBox;

import java.io.Serializable;

/**
 * Contains a constant
 * @author matteo
 */
public class Constant implements Value {
	private static final long serialVersionUID = 5701544347609041344L;

	private final Serializable value;
	private final String type;

	public Constant(Serializable v) {
		value = v;
		type = v != null ? v.getClass().getName() : null;
	}

	/**
	 * @return the value
	 */
	public Serializable getValue() {
		return value;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
}
