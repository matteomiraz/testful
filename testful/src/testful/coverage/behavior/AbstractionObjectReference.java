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

public class AbstractionObjectReference extends Abstraction {

	private static final long serialVersionUID = 2417857549496965147L;

	private final boolean isNull;

	public AbstractionObjectReference(String expression, boolean isNull) {
		super(expression);
		this.isNull = isNull;
	}

	public boolean isNull() {
		return isNull;
	}

	@Override
	public String toString() {
		return getExpression() + (isNull ? " is null" : " is not null");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isNull ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;

		if(!(obj instanceof AbstractionObjectReference)) return false;
		AbstractionObjectReference other = (AbstractionObjectReference) obj;
		if(isNull != other.isNull) return false;
		return true;
	}
}
