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

import java.util.Map;

public class AbstractorBoolean extends Abstractor {

	private static final long serialVersionUID = 1083453502190668096L;

	/**
	 * Instantiate a boolean abstraction function on the <i>expression</i> property.
	 * @param expression the expression that collects the property to abstract
	 * @param parameters the (ignored) parameters for the abstraction function
	 * @throws Exception if anything goes wrong (e.g., the expression has syntax errors)
	 */
	public AbstractorBoolean(String expression, String parameters) throws Exception {
		super(expression, parameters);
	}

	@Override
	public Abstraction get(Map<String, Object> ctx) {
		Object elem = evaluateExpression(ctx);
		return get(expression, elem);
	}

	static Abstraction get(String expression, Object elem) {
		if(elem == null) return new AbstractionObjectReference(expression, true);

		if(!(elem instanceof Boolean)) {
			logger.warning("Expected a boolean in AbstractorBoolean for expression \"" + expression +  "\"");
			return new Abstraction.AbstractionError(expression);
		}

		return new AbstractionBoolean(expression, (Boolean) elem);
	}

}
