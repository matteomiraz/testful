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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

import testful.TestFul;

/**
 * parameters = p0.getSingleObject():{p0.getCollection()}
 */
public class AbstractorRefCtx extends Abstractor {

	private static final long serialVersionUID = -8970130020923978348L;

	/** parameters that requires an equality comparison (i.e. == ) */
	private final String[] objectsString;
	/** parameters that requires a containment check (i.e. contains ) */
	private final String[] aggregatesString;

	private transient Expression[] objects;
	private transient Expression[] aggregates;

	/**
	 * Instantiate a <i>parametric</i> abstraction function on the <i>expression</i> property
	 * @param expression the expression that collects the property to abstract
	 * @param parameters the parameters for the abstraction function
	 * @throws Exception if anything goes wrong (e.g., the expression has syntax errors)
	 */
	public AbstractorRefCtx(String expression, String parameters) throws Exception {
		super(expression, parameters);

		if(parameters.length() <= 0) {
			objectsString = aggregatesString = new String[0];
			objects = aggregates = new Expression[0];
		} else {
			String[] ranges = parameters.split(":");
			List<String> tmpObj = new ArrayList<String>(ranges.length);
			List<String> tmpAggr = new ArrayList<String>(ranges.length);

			for(String s : ranges)
				if(s.startsWith("{")) tmpAggr.add(s.substring(1, s.length() - 1));
				else tmpObj.add(s);

			objectsString = tmpObj.toArray(new String[tmpObj.size()]);
			objects = new Expression[objectsString.length];
			for(int i = 0; i < objectsString.length; i++)
				objects[i] = ExpressionFactory.createExpression(objectsString[i]);

			aggregatesString = tmpAggr.toArray(new String[tmpAggr.size()]);
			aggregates = new Expression[aggregatesString.length];
			for(int i = 0; i < aggregatesString.length; i++)
				aggregates[i] = ExpressionFactory.createExpression(aggregatesString[i]);
		}
	}

	public Expression[] getObjects() {
		if(objects == null) {
			try {
				objects = new Expression[objectsString.length];
				for(int i = 0; i < objectsString.length; i++)
					objects[i] = ExpressionFactory.createExpression(objectsString[i]);
			} catch(Exception e) {
				// This should never happen!
				TestFul.debug(e);
			}
		}

		return objects;
	}

	public Expression[] getAggregates() {
		if(aggregates == null) {
			try {
				aggregates = new Expression[aggregatesString.length];
				for(int i = 0; i < aggregatesString.length; i++)
					aggregates[i] = ExpressionFactory.createExpression(aggregatesString[i]);
			} catch(Exception e) {
				// This should never happen!
				TestFul.debug(e);
			}
		}

		return aggregates;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Abstraction get(Map<String, Object> ctx) {
		Object elem = evaluateExpression(ctx);

		SortedSet<String> objs = new TreeSet<String>();

		if(elem == null) objs.add("null");

		JexlContext jc = JexlHelper.createContext();
		jc.getVars().putAll(ctx);

		for(Expression item : getObjects()) {
			try {
				Object o = item.evaluate(jc);
				if(elem == o) objs.add(item.getExpression());
			} catch(Exception e) {
				logger.log(Level.WARNING, this.getClass().getName() + ": cannot execute the JEXL query \"" + item.getExpression() + "\" : " + e.getMessage() + " due to: " + e.getCause(), e);
				TestFul.debug(e);
			}
		}

		for(Expression item : getAggregates()) {
			try {
				Object aggr = item.evaluate(jc);
				if(aggr == null) {
					logger.warning(this.getClass().getName() + ": null aggregate " + item.getExpression());
					continue;
				}

				if(aggr.getClass().isArray()) {
					for(Object o : (Object[]) aggr) {
						if(elem == o) {
							objs.add(item.getExpression());
							break;
						}
					}

				} else if(aggr instanceof Iterable<?>) {
					for(Object o : (Iterable<?>) aggr) {
						if(elem == o) {
							objs.add(item.getExpression());
							break;
						}
					}

				} else if(aggr instanceof Iterator<?>) {
					Iterator<?> iter = (Iterator<?>) aggr;
					while(iter.hasNext()) {
						if(elem == iter.next()) {
							objs.add(item.getExpression());
							break;
						}
					}

				} else {
					logger.warning(this.getClass().getName() + ": Unknown aggregate type: " + aggr.getClass().getName());
					TestFul.debug(this.getClass().getName() + ": Unknown aggregate type: " + aggr.getClass().getName());
				}
			} catch(Exception e) {
				logger.log(Level.WARNING, this.getClass().getName() + ": cannot execute the JEXL query \"" + item.getExpression() + "\" : " + e.getMessage() + " due to: " + e.getCause(), e);
				TestFul.debug(e);
			}
		}

		return AbstractionObjectRefCtx.get(expression, objs);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());

		sb.append(" objects: [");
		boolean first = true;
		for(String e : objectsString) {
			if(first) first = false;
			else sb.append(", ");
			sb.append(e);
		}
		sb.append("]");

		sb.append(" aggregates: [");
		first = true;
		for(String e : aggregatesString) {
			if(first) first = false;
			else sb.append(", ");
			sb.append(e);
		}
		sb.append("]");

		return sb.toString();
	}
}
