package testful.model;

public class OperationStatus extends OperationInformation {

	private static final long serialVersionUID = 2202341615366183486L;
	
	public static final String KEY = "STATUS";

	public static enum Status {
		NOT_EXECUTED, PRECONDITION_ERROR, POSTCONDITION_ERROR, SUCCESSFUL, EXCEPTIONAL
	}

	protected Status status;
	protected Throwable exc;

	public OperationStatus() {
		super(KEY);
		status = Status.NOT_EXECUTED;
		exc = null;
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
