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

import java.util.Arrays;

public class AbstractionMethod extends Abstraction {

	private static final long serialVersionUID = 8488220230181488768L;

	private final boolean isStatic;
	private final Abstraction[] abs;

	public AbstractionMethod(String methodName, boolean isStatic, Abstraction[] abs) {
		super(methodName);
		assert (abs != null);

		this.isStatic = isStatic;
		this.abs = abs;
	}

	public Abstraction[] getAbstractions() {
		return abs;
	}

	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getExpression()).append(" -");
		for(Abstraction a : abs)
			sb.append(" {").append(a).append("}");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(abs);
		result = prime * result + (isStatic ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;
		if(!(obj instanceof AbstractionMethod)) return false;
		AbstractionMethod other = (AbstractionMethod) obj;
		if(!Arrays.equals(abs, other.abs)) return false;
		if(isStatic != other.isStatic) return false;
		return true;
	}
}
