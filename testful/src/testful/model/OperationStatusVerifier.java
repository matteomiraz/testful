package testful.model;

public class OperationStatusVerifier extends OperationStatus {

	private static final long serialVersionUID = 3967964654671014414L;

	public OperationStatusVerifier(OperationStatus result) {
		status = result.status;
		exc = result.exc;
	}

	@Override
	public void setPreconditionError() throws OperationVerifierException {
		if(status != Status.PRECONDITION_ERROR) throw new OperationVerifierException(status, Status.PRECONDITION_ERROR);
	}

	@Override
	public void setPostconditionError() {
		if(status != Status.POSTCONDITION_ERROR) throw new OperationVerifierException(status, Status.POSTCONDITION_ERROR);
	}

	@Override
	public void setSuccessful() {
		if(status != Status.SUCCESSFUL) throw new OperationVerifierException(status, Status.SUCCESSFUL);
	}

	@Override
	public void setExceptional(Throwable exc) {
		if(status != Status.EXCEPTIONAL) throw new OperationVerifierException(status, Status.EXCEPTIONAL);

		Throwable thisExc = this.exc;
		if(!thisExc.getClass().equals(exc.getClass()) || !thisExc.getMessage().equals(exc.getMessage())) throw new OperationVerifierException(thisExc, exc);
	}

	@Override
	public OperationInformation clone() {
		return new OperationStatusVerifier(this);
	}

	@Override
	public String toString() {
		return "(verified) " + super.toString();
	}

	public static void insertOperationStatusVerifier(Operation[] ops) {
		Test.ensureNoDuplicateOps(ops);

		for(Operation op : ops) {
			OperationStatus res = (OperationStatus) op.getInfo(OperationStatus.KEY);
			if(res != null) op.addInfo(new OperationStatusVerifier(res));
		}
	}

	public static class OperationVerifierException extends FaultyExecutionException {

		private static final long serialVersionUID = -5320352798948137983L;

		private OperationVerifierException(String msg) {
			super(msg, null);
		}

		public OperationVerifierException(Status expected, Status actual) {
			this("Operation Verifier: expected " + expected + ", actual: " + actual);
		}

		public OperationVerifierException(Throwable expected, Throwable actual) {
			this("Operation Verifier: operation termiated with a wrong exception. Expected " + expected + ", actual: " + actual);
		}
	}
}
