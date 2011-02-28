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
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

import testful.TestFul;

/**
 * Abstract a number using an user-provided domain subdivision. The user can use
 * the "extra" field of the Observer annotation, and provide his own division of
 * the number domain. The syntax of the following:
 * <code><value>(:&lt;value&gt;)*</code> For example, if the user specifies:
 * <code>-1:0:target:100</code> the input domain is divided in the following
 * part:
 * <ul>
 * <li>(-Infinity, -1)</li>
 * <li>-1</li>
 * <li>(-1, 0)</li>
 * <li>0</li>
 * <li>(0, target) <i>where target's value can be observed either through a
 * public field or a getter observer. Its value can vary between 0 and 100</i></li>
 * <li>target</li>
 * <li>(target, 100)</li>
 * <li>100</li>
 * <li>(100, +Infinity) <i>notice that the last semicolumn is missing, so this
 * interval is created iff a dynamic execution is observed</i></li>
 * 
 * @author matteo
 */
public class AbstractorNumber extends Abstractor {

	private static final long serialVersionUID = 6779751668245885518L;

	public static Abstraction get(String expression, Object obj) {
		if(!(obj instanceof Number)) {
			logger.warning("Expected a number in AbstractorNumber for expression \"" + expression + "\"");
			return new Abstraction.AbstractionError(expression);
		}

		double elem = ((Number) obj).doubleValue();

		if(Double.isInfinite(elem)) {
			if(elem > 0) return new AbstractionNumber(expression, expression + " is " + AbstractionNumber.P_INF);
			else return new AbstractionNumber(expression, expression + " is " + AbstractionNumber.N_INF);
		} else if(Double.isNaN(elem)) return new AbstractionNumber(expression, expression + " is " + AbstractionNumber.NaN);
		else if(elem < 0) return new AbstractionNumber(expression, expression + AbstractionNumber.NEGATIVE);
		else if(elem == 0) return new AbstractionNumber(expression, expression + AbstractionNumber.ZERO);
		else return new AbstractionNumber(expression, expression + AbstractionNumber.POSITIVE);
	}

	private final String[] intervalsString;
	private transient Expression[] intervals;

	/**
	 * Instantiate a <i>parametric</i> abstraction function on the <i>expression</i> property.
	 * @param expression the expression that collects the property to abstract
	 * @param parameters the parameters for the abstraction function
	 * @throws Exception if anything goes wrong (e.g., the expression has syntax errors)
	 */
	public AbstractorNumber(String expression, String parameters) throws Exception {
		super(expression, parameters);

		parameters = parameters.trim();

		if(parameters.length() <= 0) {
			intervalsString = null;
			intervals = null;
		} else {
			intervalsString = parameters.split(":");

			intervals = new Expression[intervalsString.length];
			for(int i = 0; i < intervalsString.length; i++)
				intervals[i] = ExpressionFactory.createExpression(intervalsString[i]);
		}
	}

	private Expression[] getIntervals() {
		if(intervals == null && intervalsString != null) {
			try {
				intervals = new Expression[intervalsString.length];
				for(int i = 0; i < intervalsString.length; i++)
					intervals[i] = ExpressionFactory.createExpression(intervalsString[i]);
			} catch(Exception e) {
				TestFul.debug("This should never happen", e);
			}
		}

		return intervals;
	}

	@Override
	public Abstraction get(Map<String, Object> ctx) {
		// evaluate the expression and retrieve the result (obj)
		Object obj = evaluateExpression(ctx);

		if(obj == null) return new AbstractionObjectReference(expression, true);

		if(!(obj instanceof Number)) {
			logger.warning("Expected a number in AbstractorNumber!");
			return new Abstraction.AbstractionError(expression);
		}

		// if there are no intervals, then use the "default" abstraction
		if(intervalsString == null) return get(expression, obj);

		double elem = ((Number) obj).doubleValue();

		// ensure that this.intervals contains parsed expressions
		getIntervals();

		// retrieve the value of intervals. it may contain null values
		Double[] values = evaluateIntervals(ctx);

		// if the value is NaN, calculate the return value
		if(Double.isNaN(elem)) {
			String label = null;
			for(int i = 0; i < values.length; i++)
				if(values[i] != null && Double.isNaN(values[i])) if(label == null) label = intervalsString[i];
				else label += "," + intervalsString[i];

			if(label == null) return new AbstractionNumber(expression, expression + " is " + AbstractionNumber.NaN);
			return new AbstractionNumber(expression, expression + " = " + label);
		}

		// build the sortedValues Map
		SortedMap<Double, String> sortedValues = new TreeMap<Double, String>();
		for(int i = 0; i < values.length; i++)
			if(values[i] != null && !Double.isNaN(values[i])) {
				String label = sortedValues.get(values[i]);
				if(label == null) label = intervalsString[i];
				else label += "," + intervalsString[i];

				sortedValues.put(values[i], label);
			}
		if(!sortedValues.containsKey(Double.POSITIVE_INFINITY)) sortedValues.put(Double.POSITIVE_INFINITY, AbstractionNumber.P_INF);
		if(!sortedValues.containsKey(Double.NEGATIVE_INFINITY)) sortedValues.put(Double.NEGATIVE_INFINITY, AbstractionNumber.N_INF);

		// calculate the interval
		String prev = null;
		for(Entry<Double, String> entry : sortedValues.entrySet()) {
			if(prev != null && elem < entry.getKey()) return new AbstractionNumber(expression, prev + " < " + expression + " < " + entry.getValue());
			if(elem == entry.getKey()) return new AbstractionNumber(expression, expression + " = " + entry.getValue());
			prev = entry.getValue();
		}

		if(TestFul.DEBUG) TestFul.debug("This point was not supposed to be reachable.");
		return new AbstractionNumber(expression, expression + " = " + AbstractionNumber.P_INF);
	}

	@SuppressWarnings("unchecked")
	private Double[] evaluateIntervals(Map<String, Object> ctx) {

		JexlContext jc = JexlHelper.createContext();
		jc.getVars().putAll(ctx);

		Expression[] intervals = getIntervals();

		Double[] ret = new Double[intervals.length];
		for(int i = 0; i < intervals.length; i++)
			try {
				if(intervals[i] != null) {
					Number evaluate = (Number) intervals[i].evaluate(jc);
					if(evaluate != null) ret[i] = evaluate.doubleValue();
				}
			} catch(Exception e) {
				logger.log(Level.WARNING, "Cannot execute the JEXL query \"" + intervals[i].getExpression() + "\" : " + e.getMessage() + " due to: " + e.getCause(), e);
			}

			return ret;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());

		if(intervalsString == null) sb.append(" by sign");
		else {
			sb.append(" [");

			boolean first = true;
			for(String e : intervalsString) {
				if(first) first = false;
				else sb.append(", ");
				sb.append(e);
			}
			sb.append("]");
		}

		return sb.toString();
	}
}
