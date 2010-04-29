package testful.model;

/**
 * Tracks the status of an operation, recording whether it is
 * <ul>
 *   <li>invalid (PRECONDITION_ERROR)</li>
 *   <li>faulty (POSTCONDITION_ERROR)</li>
 *   <li>ok, without exception (SUCCESSFUL)</li>
 *   <li>ok, throwing exceptions (EXCEPTIONAL)</li>
 * </ul>
 * @author matteo
 */
public class OperationStatus extends OperationInformation {

	private static final long serialVersionUID = 2202341615366183486L;

	public static final String KEY = "STATUS";

	public static enum Status {
		/** The operation has not been executed */
		NOT_EXECUTED,

		/** the operation is invalid */
		PRECONDITION_ERROR,

		/** the operation is faulty */
		POSTCONDITION_ERROR,

		/** the operation is ok: it terminates without throwing any exception */
		SUCCESSFUL,

		/** the operation is ok: it terminates throwing an exception */
		EXCEPTIONAL
	}

	protected Status status;
	protected Throwable exc;

	public OperationStatus() {
		super(KEY);
		status = Status.NOT_EXECUTED;
		exc = null;
	}

	private OperationStatus(Status status, Throwable exc) {
		super(KEY);
		this.status = status;
		this.exc = exc;
	}

	public Status getStatus() {
		return status;
	}

	public void setPreconditionError() {
		status = Status.PRECONDITION_ERROR;
		exc = null;
	}

	public void setPostconditionError() {
		status = Status.POSTCONDITION_ERROR;
		exc = null;
	}

	public void setSuccessful() {
		status = Status.SUCCESSFUL;
	}

	public void setExceptional(Throwable exc) {
		status = Status.EXCEPTIONAL;
		this.exc = exc;
	}

	public Throwable getException() {
		return exc;
	}

	@Override
	public OperationInformation clone() {
		return new OperationStatus(status, exc);
	}

	@Override
	public String toString() {
		return status + (exc != null ? " (" + exc + ")" : "");
	}

	public static void insert(Operation[] ops) {
		Test.ensureNoDuplicateOps(ops);

		for(Operation op : ops)
			if(op instanceof Invoke || op instanceof CreateObject) op.addInfo(new OperationStatus());
	}

	public static void remove(Test t) {
		for(Operation op : t.getTest())
			op.removeInfo(OperationStatus.KEY);
	}
}
