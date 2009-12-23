package testful.coverage.behavior;

import java.io.Serializable;
import java.util.Map;

/**
 * @author matteo
 */
public class AbstractorMethod implements Serializable {

	private static final long serialVersionUID = 1335341464192577720L;

	private final boolean isStatic;
	private final String methodName;

	private final Abstractor[] state;

	public AbstractorMethod(String methodName, boolean isStatic) {
		this.isStatic = isStatic;
		this.methodName = methodName;
		state = null;
	}

	public AbstractorMethod(String methodName, boolean isStatic, Abstractor[] state) {
		this.isStatic = isStatic;
		this.methodName = methodName;
		this.state = state;
	}

	public AbstractionMethod get(Map<String, Object> ctx) {
		if(state == null) return new AbstractionMethod(methodName, isStatic, new Abstraction[0]);
		else {
			int n = 0;
			Abstraction[] abs = new Abstraction[state.length];
			for(Abstractor abstractor : state)
				abs[n++] = abstractor.get(ctx);

			return new AbstractionMethod(methodName, isStatic, abs);
		}
	}
}
