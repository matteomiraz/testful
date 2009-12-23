package testful.coverage.behavior;

import java.util.Map;

public class AbstractorBoolean extends Abstractor {

	private static final long serialVersionUID = 1083453502190668096L;

	public AbstractorBoolean(String value, String range) throws Exception {
		super(value, range);
	}

	@Override
	public Abstraction get(Map<String, Object> ctx) {
		Object elem = evaluateExpression(ctx);
		return get(expression, elem);
	}

	static Abstraction get(String expression, Object elem) {
		if(elem == null) return new AbstractionObjectReference(expression, true);

		if(!Boolean.TYPE.equals(elem.getClass()) && !Boolean.class.equals(elem.getClass())) {
			System.err.println("ERR: expected a boolean in AbstractorBoolean!");
			return new Abstraction.AbstractionError(expression);
		}

		return new AbstractionBoolean(expression, (Boolean) elem);
	}

}
