package testful.model;



public class OperationResultVerifier extends OperationResult {
	private static final long serialVersionUID = -1087900671239338703L;

	public OperationResultVerifier(OperationResult op) {
		object = op.object;
		result = op.result;
	}

	@Override
	public void setValue(Object object, Object result, TestCluster cluster) throws FaultyExecutionException {
		this.object.check(new Value(object, cluster));
		this.result.check(new Value(result, cluster));
	}

	public static void insertOperationResultVerifier(Operation[] ops) {
		Test.ensureNoDuplicateOps(ops);

		for(Operation op : ops) {
			OperationResult res = (OperationResult) op.removeInfo(OperationResult.KEY);
			if(res != null) op.addInfo(new OperationResultVerifier(res));
		}
	}
}
