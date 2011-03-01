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

import java.io.Serializable;

public abstract class Abstraction implements Serializable {

	private static final long serialVersionUID = -5699038898763653554L;

	private String expression;

	/**
	 * Default constructor
	 * 
	 * @param expression : the abstracted expression (e.g. "this.size()")
	 */
	public Abstraction(String expression) {
		this.expression = expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof Abstraction)) return false;

		Abstraction other = (Abstraction) obj;
		if(expression == null) return other.expression == null;
		return expression.equals(other.expression);
	}

	@Override
	public abstract String toString();

	static final class AbstractionError extends Abstraction {

		private static final long serialVersionUID = 4015057413935518817L;

		public AbstractionError(String expression) {
			super(expression);
		}

		@Override
		public String toString() {
			return getExpression() + " -> evaluation error;";
		}
	}
}
