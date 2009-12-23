package testful.coverage.behavior;

import java.util.Map;

public class AbstractorString extends Abstractor {

	private static final long serialVersionUID = 5624401341659286922L;

	public AbstractorString(String value, String range) throws Exception {
		super(value, range);
	}

	@Override
	public Abstraction get(Map<String, Object> ctx) {
		Object elem = evaluateExpression(ctx);
		return get(expression, elem);
	}

	static Abstraction get(String expression, Object obj) {
		if(obj == null) return new AbstractionObjectReference(expression, true);

		if(!(obj instanceof String)) {
			System.err.println("ERR: expected a String in AbstractorString!");
			return new Abstraction.AbstractionError(expression);
		}

		String string = (String) obj;

		return new AbstractionStringEmpty(expression, string.trim().length() == 0);
	}
}
