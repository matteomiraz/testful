package testful.model;

public class InternalPreConditionViolationException extends FaultyExecutionException {

	private static final long serialVersionUID = -1297683563545327991L;

	public InternalPreConditionViolationException() {
		super("", null);
	}

	public InternalPreConditionViolationException(String msg, Throwable exc) {
		super(msg, exc);
	}
}
