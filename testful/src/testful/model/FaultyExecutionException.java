package testful.model;

/**
 * This exception represents a faulty execution of the class under testing.<br>
 * Internally, this exception stores the stacktrace of the fault, and thus it is
 * possible to compare two instances of this exception and find out if them are
 * manifestations of the same bug or not.
 * 
 * @author matteo
 */
public abstract class FaultyExecutionException extends RuntimeException {

	private static final long serialVersionUID = -6674159663143126864L;

	public FaultyExecutionException(String msg, Throwable e) {
		super(msg, e);
	}
}
