package testful.model;

public class ExceptionRaisedException extends FaultyExecutionException {

	private static final long serialVersionUID = -2171152306968557667L;

	public ExceptionRaisedException(Throwable e) {
		super(null, e);
	}

	public ExceptionRaisedException(String msg, Throwable e) {
		super(msg, e);
	}
}
