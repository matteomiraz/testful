package testful.model;


public class OperationResultVerifier extends OperationResult {
	private static final long serialVersionUID = -1087900671239338703L;

	public OperationResultVerifier(OperationResult op) {
		object = op.object;
		result = op.result;
	}

	@Override
	public void setValue(Object object, Object result, TestCluster cluster) throws FaultyExecutionException {
		Value newObject = new Value(object, cluster);
		if(!newObject.equals(this.object))
			throw new OperationResultVerifierException(this.object, newObject);

		Value newResult = new Value(result, cluster);
		if(!newResult.equals(this.result))
			throw new OperationResultVerifierException(this.result, newResult);
	}

	public static void insertOperationResultVerifier(Operation[] ops) {
		Test.ensureNoDuplicateOps(ops);

		for(Operation op : ops) {
			OperationResult res = (OperationResult) op.removeInfo(OperationResult.KEY);
			if(res != null) op.addInfo(new OperationResultVerifier(res));
		}
	}

	public static class OperationResultVerifierException extends FaultyExecutionException {
		private static final long serialVersionUID = -9113533247815125403L;

		private final Value expected;
		private final Value actual;

		public OperationResultVerifierException(Value expected, Value actual) {
			super("Operation result verifier: expected: " + expected + " actual: " + actual, null);
			this.expected = expected;
			this.actual = actual;
		}

		public Value getExpected() {
			return expected;
		}

		public Value getActual() {
			return actual;
		}
	}

}
