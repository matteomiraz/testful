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

public class AbstractionBoolean extends Abstraction {

	private static final long serialVersionUID = -1735480378740327016L;

	private final boolean value;

	AbstractionBoolean(String expression, boolean value) {
		super(expression);
		this.value = value;
	}

	@Override
	public String toString() {
		if(value) return getExpression() + ": True";
		else return getExpression() + ": False";
	}

	@Override
	public int hashCode() {
		return 31 * super.hashCode() + (value ? 1231 : 1237);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;
		if(!(obj instanceof AbstractionBoolean)) return false;
		AbstractionBoolean other = (AbstractionBoolean) obj;
		if(value != other.value) return false;
		return true;
	}

}
