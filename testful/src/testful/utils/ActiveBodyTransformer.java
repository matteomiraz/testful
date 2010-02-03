package testful.utils;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;

public class ActiveBodyTransformer extends BodyTransformer {

	public static BodyTransformer v(BodyTransformer delegate) {
		return new ActiveBodyTransformer(delegate);
	}

	private final BodyTransformer delegate;

	private ActiveBodyTransformer(BodyTransformer delegate) {
		this.delegate = delegate;
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void internalTransform(Body body, String phaseName, Map options) {
		delegate.transform(body.getMethod().getActiveBody(), phaseName, options);
	}

}
