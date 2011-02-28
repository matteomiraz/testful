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
import java.util.Map;

/**
 * Method abstractor.
 * It leverages abstractors to provide an abstracted view on parameters.
 * @author matteo
 */
public class AbstractorMethod implements Serializable {

	private static final long serialVersionUID = 1335341464192577720L;

	/** the name of the method being abstracted */
	private final String methodName;

	/** whether the method is static or not */
	private final boolean isStatic;

	/** the abstractors to use to discretize actual parameters */
	private final Abstractor[] parameters;

	/**
	 * Creates a new method abstractor
	 * @param methodName the name of the method
	 * @param isStatic whether the method is static or not
	 * @param parameters the abstractors to use to discretize actual parameters (null is valid, and it is translated in an empty array)
	 */
	public AbstractorMethod(String methodName, boolean isStatic, Abstractor[] parameters) {
		this.isStatic = isStatic;
		this.methodName = methodName;

		if(parameters == null) this.parameters = new Abstractor[0];
		else this.parameters = parameters;
	}

	/**
	 * Perform the method abstraction.
	 * @param ctx the context, initialized both with this (if the method is not static) and parameters (p0, p1, ...)
	 * @return the method abstraction
	 */
	public AbstractionMethod get(Map<String, Object> ctx) {
		Abstraction[] abs = new Abstraction[parameters.length];
		for (int i = 0; i < parameters.length; i++)
			abs[i] = parameters[i].get(ctx);

		return new AbstractionMethod(methodName, isStatic, abs);
	}
}
