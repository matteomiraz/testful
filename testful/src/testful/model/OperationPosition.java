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

package testful.model;

/**
 * Memorize the ordinal position of an operation in a test
 * @author matteo
 */
public class OperationPosition extends OperationInformation {
	private static final long serialVersionUID = 3664462416048405563L;

	public static String KEY = "OperationPosition";
	public final int position;

	public OperationPosition(int position) {
		super(KEY);
		this.position = position;
	}

	@Override
	public String toString() {
		return "Operation #" + Integer.toString(position);
	}

	@Override
	public OperationInformation clone() {
		return this;
	}

}