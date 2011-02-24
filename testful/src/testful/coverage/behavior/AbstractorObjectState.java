package testful.coverage.behavior;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AbstractorObjectState implements Serializable {

	private static final long serialVersionUID = -9199070129791166496L;

	private final Abstractor[] state;

	public AbstractorObjectState(Abstractor[] state) {
		this.state = state;
	}

	public Abstraction get(Object _this) {
		if(_this == null) return new AbstractionObjectReference("", true);

		// update ctx
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("this", _this);

		int n = 0;
		Abstraction[] abs = new Abstraction[state.length];
		for(Abstractor abstractor : state)
			abs[n++] = abstractor.get(ctx);

		return new AbstractionObjectState(_this.getClass().getCanonicalName(), abs);
	}
}
