package testful.model;

public class InvariantViolationException extends FaultyExecutionException {

	private static final long serialVersionUID = -3934943940883802263L;

	public InvariantViolationException(String msg, Throwable exc) {
		super(msg, exc);
	}
}
