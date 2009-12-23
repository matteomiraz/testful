package testful.coverage.behavior;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

/**
 * Extra = p0.getSingoloOggetto():{p0.getCollezione()}
 */
public class AbstractorRefCtx extends Abstractor {

	private static final long serialVersionUID = -8970130020923978348L;

	private final String[] objectsString;
	private final String[] aggregatesString;

	private transient Expression[] objects;
	private transient Expression[] aggregates;

	private AbstractorRefCtx(String value, String range) throws Exception {
		super(value, range);

		if(range.length() <= 0) {
			objectsString = aggregatesString = new String[0];
			objects = aggregates = new Expression[0];
		} else {
			String[] ranges = range.split(":");
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
		if(objects == null) try {
			objects = new Expression[objectsString.length];
			for(int i = 0; i < objectsString.length; i++)
				objects[i] = ExpressionFactory.createExpression(objectsString[i]);
		} catch(Exception e) {
			// This should never happen!
			e.printStackTrace();
		}

		return objects;
	}

	public Expression[] getAggregates() {
		if(aggregates == null) try {
			aggregates = new Expression[aggregatesString.length];
			for(int i = 0; i < aggregatesString.length; i++)
				aggregates[i] = ExpressionFactory.createExpression(aggregatesString[i]);
		} catch(Exception e) {
			// This should never happen!
			e.printStackTrace();
		}

		return aggregates;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Abstraction get(Map<String, Object> ctx) {
		Object elem = evaluateExpression(ctx);

		SortedSet<String> objs = new TreeSet<String>();

		JexlContext jc = JexlHelper.createContext();
		jc.getVars().putAll(ctx);

		for(Expression item : getObjects())
			try {
				Object o = item.evaluate(jc);
				if(elem == o) objs.add(item.getExpression());
			} catch(Exception e) {
				System.err.println("ERR: (" + this.getClass().getCanonicalName() + ") cannot execute the JEXL query \"" + item.getExpression() + "\" : " + e.getMessage() + " due to: " + e.getCause());
				e.printStackTrace();
			}

			for(Expression item : getAggregates())
				try {
					Object aggr = item.evaluate(jc);
					if(aggr == null) {
						System.err.println("WARN: null aggregate " + item.getExpression());
						continue;
					}

					if(aggr.getClass().isArray()) {
						for(Object o : (Object[]) aggr)
							if(elem == o) objs.add(item.getExpression());
					} else if(aggr instanceof Iterable<?>) {
						for(Object o : (Iterable<?>) aggr)
							if(elem == o) objs.add(item.getExpression());
					} else if(aggr instanceof Iterator<?>) {
						Iterator<?> iter = (Iterator<?>) aggr;
						while(iter.hasNext())
							if(elem == iter.next()) objs.add(item.getExpression());
					} else System.err.println("Unknown aggregate type: " + aggr.getClass().getCanonicalName());
				} catch(Exception e) {
					System.err.println("ERR: (" + this.getClass().getCanonicalName() + ") cannot execute the JEXL query \"" + item.getExpression() + "\" : " + e.getMessage() + " due to: " + e.getCause());
					e.printStackTrace();
				}

				for(String s : ctx.keySet())
					if(elem == ctx.get(s)) objs.add(s);

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
