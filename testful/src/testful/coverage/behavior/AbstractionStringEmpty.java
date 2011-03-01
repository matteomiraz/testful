/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011  Matteo Miraz
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

package testful.coverage.behavior;

public class AbstractionStringEmpty extends Abstraction {

	private static final long serialVersionUID = -2491111474194919857L;

	private final boolean empty;

	public AbstractionStringEmpty(String expression, boolean empty) {
		super(expression);
		this.empty = empty;
	}

	@Override
	public String toString() {
		return getExpression() + ": " + (empty ? "empty" : "non-empty") + " string";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (empty ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;

		if(!(obj instanceof AbstractionStringEmpty)) return false;
		AbstractionStringEmpty other = (AbstractionStringEmpty) obj;
		if(empty != other.empty) return false;

		return true;
	}

}
