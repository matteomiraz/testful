package testful.coverage.behavior;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

import testful.coverage.Tracker;

/**
 * This is the default abstractor. Depending on the type of the object being
 * abstracted, it uses
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

	private static final long serialVersionUID = 9001081059121196419L;

	private transient Expression expr;
	protected final String expression;

	public Abstractor(String value, String range) throws Exception {
		expression = value;

		// parse the query
		expr = ExpressionFactory.createExpression(value);
	}

	public Expression getExpr() {
		if(expr == null) try {
			expr = ExpressionFactory.createExpression(expression);
		} catch(Exception e) {
			// This should never happen
			e.printStackTrace();
		}
		return expr;
	}

	public Abstraction get(Map<String, Object> ctx) {
		Object elem = evaluateExpression(ctx);

		if(elem == null) return new AbstractionObjectReference(expression, true);

		Class<? extends Object> type = elem.getClass();

		if(Boolean.TYPE.equals(type) || Boolean.class.equals(type)) return AbstractorBoolean.get(expression, elem);

		if(type == Short.TYPE || type == Integer.TYPE || type == Long.TYPE || type == Float.TYPE || type == Double.TYPE || Number.class.isAssignableFrom(type)) return AbstractorNumber.get(expression,
				elem);

		if(elem instanceof String) return AbstractorString.get(expression, elem);

		BehaviorTrackerData data = (BehaviorTrackerData) Tracker.getDatum(BehaviorTrackerData.KEY);
		return data.getAbstractorClass(elem.getClass().getCanonicalName()).get(elem);
	}

	@SuppressWarnings("unchecked")
	protected Object evaluateExpression(Map<String, Object> ctx) {
		JexlContext jc = JexlHelper.createContext();
		jc.getVars().putAll(ctx);

		try {
			return getExpr().evaluate(jc);
		} catch(Exception e) {
			System.err.println("ERR: cannot evaluate the JEXL expression \"" + expression + "\" : " + e.getMessage() + " due to: " + e.getCause());
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getCanonicalName();
	}
}
