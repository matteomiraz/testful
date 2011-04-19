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

package testful.model.xml.behavior;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstraction", namespace = "http://testful.sourceforge.net/schema/2.0/behavioral.xsd")
public class Abstraction {

	/** the abstraction function to use */
	@XmlAttribute(required = true)
	protected String function;

	/** the expression to abstract */
	@XmlAttribute(required = true)
	protected String expression;

	/** the parameters for parametric abstraction functions */
	@XmlAttribute(required = false)
	protected String parameters = "";

	/**
	 * Returns the abstraction function to use
	 * @return the abstraction function to use
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * Sets the abstraction function to use
	 * @param function the abstraction function to use
	 */
	public void setFunction(String function) {
		this.function = function;
	}

	/**
	 * Returns the expression to abstract
	 * @return the expression to abstract
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Sets the expression to abstract
	 * @param expression the expression to abstract
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * Returns the parameters for parametric abstraction functions
	 * @return the parameters for parametric abstraction functions
	 */
	public String getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters for parametric abstraction functions
	 * @param parameters the parameters for parametric abstraction functions
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "function:" + function + " expr:" + expression + " params:" + parameters;
	}
}
