package testful.model;

public class PostConditionViolationException extends FaultyExecutionException {

	private static final long serialVersionUID = -5000838658322301775L;

	public PostConditionViolationException() {
		super("", null);
	}

	public PostConditionViolationException(String msg, Throwable exc) {
		super(msg, exc);
	}
}
