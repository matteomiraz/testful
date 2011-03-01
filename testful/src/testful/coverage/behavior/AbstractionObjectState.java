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

public class AbstractionObjectState extends Abstraction {

	private static final long serialVersionUID = -4782880343029591499L;

	private Abstraction[] abstraction;

	public AbstractionObjectState(String type, Abstraction[] abstraction) {
		super(type);

		if(abstraction != null) this.abstraction = abstraction;
		else this.abstraction = new Abstraction[0];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getExpression()).append(":");

		for(Abstraction a : abstraction)
			sb.append(" {").append(a.toString()).append("}");

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(abstraction);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;

		if(!(obj instanceof AbstractionObjectState)) return false;
		AbstractionObjectState other = (AbstractionObjectState) obj;
		if(!Arrays.equals(abstraction, other.abstraction)) return false;

		return true;
	}

}
