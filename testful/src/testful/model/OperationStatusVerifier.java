package testful.model;

import java.util.logging.Logger;

public class OperationStatusVerifier extends OperationStatus {
	private static Logger logger = Logger.getLogger("testful.executor.OperationStatusVerifier");

	private static final long serialVersionUID = 3967964654671014414L;

	public OperationStatusVerifier(OperationStatus result) {
		status = result.status;
		exc = result.exc;
	}

	@Override
	public void setPreconditionError() throws OperationVerifierException {
		if(status != Status.PRECONDITION_ERROR) {
			logger.finer("Failing tests: expected" + status + ", got: " + Status.PRECONDITION_ERROR);
			throw new OperationVerifierException(status, Status.PRECONDITION_ERROR);
		}
	}

	@Override
	public void setPostconditionError() {
		if(status != Status.POSTCONDITION_ERROR) {
			logger.finer("Failing tests: expected" + status + ", got: " + Status.POSTCONDITION_ERROR);
			throw new OperationVerifierException(status, Status.POSTCONDITION_ERROR);
		}
	}

	@Override
	public void setSuccessful() {
		if(status != Status.SUCCESSFUL) {
			logger.finer("Failing tests: expected" + status + ", got: " + Status.SUCCESSFUL);
			throw new OperationVerifierException(status, Status.SUCCESSFUL);
		}
	}

	@Override
	public void setExceptional(Throwable exc) {
		if(status != Status.EXCEPTIONAL) {
			logger.finer("Failing tests: expected" + status + ", got: " + Status.EXCEPTIONAL);
			throw new OperationVerifierException(status, Status.EXCEPTIONAL);
		}

		if(this.exc == null && exc == null) return;

		if(this.exc == null || exc == null) {
			logger.finer("Failing tests: expected exception " + this.exc + ", got: " + exc);
			throw new OperationVerifierException(this.exc, exc);
		}

		if(!this.exc.getClass().equals(exc.getClass()) ||
				(this.exc.getMessage() == null && exc.getMessage() != null) ||
				(this.exc.getMessage() != null && !this.exc.getMessage().equals(exc.getMessage()))) {
			logger.finer("Failing tests: expected exception " + this.exc.getClass().getCanonicalName() + " (" + this.exc.getMessage() + ")"
					+ " got: " + exc.getClass().getCanonicalName() + " (" + exc.getMessage() + ")");
			throw new OperationVerifierException(this.exc, exc);
		}
	}

	@Override
	public String toString() {
		return "(verified) " + super.toString();
	}

	public static void insertOperationStatusVerifier(Operation[] ops) {
		Test.ensureNoDuplicateOps(ops);

		for(Operation op : ops) {
			OperationStatus res = (OperationStatus) op.removeInfo(OperationStatus.KEY);
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
