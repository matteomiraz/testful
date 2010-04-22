package testful.model;


public class OperationResultPruner extends OperationResult {
	private static final long serialVersionUID = -1087900671239338703L;

	public OperationResultPruner(OperationResult op) {
		object = op.object;
		result = op.result;
	}

	@Override
	public void setValue(Object object, Object result, TestCluster cluster) throws FaultyExecutionException {
		this.object = this.object.prune(new Value(object, cluster));
		this.result = this.result.prune(new Value(result, cluster));
	}

	public static void insertOperationResultPruner(Operation[] ops) {
		Test.ensureNoDuplicateOps(ops);

		for(Operation op : ops) {
			OperationResult res = (OperationResult) op.removeInfo(OperationResult.KEY);
			if(res != null) op.addInfo(new OperationResultPruner(res));
		}
	}
}
