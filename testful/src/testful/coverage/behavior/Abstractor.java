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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

/**
 * This is the default abstractor.
 * It performs a dynamic abstraction selection by considering the type of the object being abstracted:Depending on the type of the object being
 * <ul>
 * <li><i>AbstractionObjectReference</i> if the object is <b>null</b></li>
 * <li><i>AbstractorBoolean</i> if the object is a <b>boolean</b></li>
 * <li><i>AbstractorNumber</i> if the object is a <b>Number</b></li>
 * <li><i>AbstractorString</i> if the object is <b>String</b></li>
 * <li><i>AbstractorObjectState</i> otherwise</li>
 * </ul>
 * 
 * @author matteo
 */
public class Abstractor implements Serializable {

	protected static Logger logger = Logger.getLogger("testful.coverage.behavior");

	private static final long serialVersionUID = 9001081059121196419L;

	private transient Expression expr;
	protected final String expression;

	/**
	 * Instantiate a <i>parametric</i> abstraction function on the <i>expression</i> property.<br/>
	 * <b>Each Abstractor MUST provide a constructor with two strings, even if it does not have parameters</b>
	 * @param expression the expression that collects the property to abstract
	 * @param parameters the parameters for the abstraction function (the Abstractor's implementation ignores it).
	 * @throws Exception if anything goes wrong (e.g., the expression has syntax errors)
	 */
	public Abstractor(String expression, String parameters) throws Exception {
		this.expression = expression;

		// parse the query
		expr = ExpressionFactory.createExpression(expression);
	}

	/**
	 * Returns the property being abstracted
	 * @return the property being abstracted
	 */
	public Expression getExpr() {
		if(expr == null) try {
			expr = ExpressionFactory.createExpression(expression);
		} catch(Exception e) {
			// This should never happen
			e.printStackTrace();
		}
		return expr;
	}

	/**
	 * Run the abstraction on the specified context
	 * @param ctx the context on the abstraction,
	 * 			  which specifies the <i>this</i> and
	 *            the actual parameters (<i>p0</i>, <i>p1</i>, ...)
	 * @return the abstraction
	 */
	public Abstraction get(Map<String, Object> ctx) {
		Object elem = evaluateExpression(ctx);

		if(elem instanceof Boolean) return AbstractorBoolean.get(expression, elem);

		if(elem instanceof Number) return AbstractorNumber.get(expression, elem);

		if(elem instanceof String) return AbstractorString.get(expression, elem);

		return BehaviorTracker.getTracker().abstractState(elem);
	}

	@SuppressWarnings("unchecked")
	protected Object evaluateExpression(Map<String, Object> ctx) {
		JexlContext jc = JexlHelper.createContext();
		jc.getVars().putAll(ctx);

		try {
			return getExpr().evaluate(jc);
		} catch(Exception e) {
			logger.log(Level.WARNING, "cannot evaluate the JEXL expression \"" + expression + "\" : " + e.getMessage() + " due to: " + e.getCause(), e);
			return null;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
