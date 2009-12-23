package testful.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import testful.coverage.TrackerDatum;
import testful.runner.ClassFinder;
import testful.runner.Context;
import testful.runner.IRunner;

/**
 * Simplify a test, by
 * <ul>
 * <li>removing operations with false preconditions (this reduces the contract
 * coverage)</li>
 * <li>removing dependencies through primitive fields.<br>
 * For example, 
 * <ol>
 * <li>integer_1 = 10;
 * <li>integer_0 = Math.abs(integer_1); 
 * <li>foo(integer_0);
 * </ol>
 * becomes
 * <ol>
 * <li>integer_1 = 10;</li>
 * <li>integer_0 = Math.abs(integer_1);</li>
 * <li>integer_0 = 10;</li>
 * <li>foo(integer_0);</li>
 * </ul>
 * </li>
 * 
 * @author matteo
 */
public class TestSimplifier {
	private static Logger logger = Logger.getLogger("testful.model");

	private final IRunner executor;
	private final ClassFinder finder;
	private final TrackerDatum[] data;

	/**
	 * Initialize a Test Simplifier.<br>
	 * This class relies on the following Operation Information:
	 * <ul>
	 * <li>OperationStatus</li>
	 * <li>OperationPrimitiveResult</li>
	 * </ul>
	 * If tests will not have those information, set execute to true: this way,
	 * tests will be executed to retrieve that information.
	 */
	public TestSimplifier(TrackerDatum ... data) {
		executor = null;
		finder = null;
		this.data = data;
	}

	public TestSimplifier(IRunner executor, ClassFinder finder, TrackerDatum ... data) {
		this.executor = executor;
		this.finder = finder;
		this.data = data;
	}

	public Test analyze(Test test) {
		Operation[] testOperations = null;
		if(executor != null) {
			OperationPrimitiveResult.insert(test.getTest());
			OperationStatus.insert(test.getTest());
			Context<Operation[], TestExecutionManager> ctx = TestExecutionManager.getContext(finder, test, data);
			ctx.setStopOnBug(false);
			Future<Operation[]> result = executor.execute(ctx);

			try {
				testOperations = result.get();
			} catch(Exception e) {
				logger.warning(e.getMessage());
			}
		}

		if(testOperations == null) {
			// clone the operations
			Operation[] originalOps = test.getTest();
			Operation[] ops = new Operation[originalOps.length];
			for(int i = 0; i < originalOps.length; i++)
				ops[i] = originalOps[i].clone();

			Test copy = new Test(test.getCluster(), test.getReferenceFactory(), ops);
			copy.ensureNoDuplicateOps();
			testOperations = copy.getTest();
		}

		List<Operation> ops = new ArrayList<Operation>();
		for(Operation op : testOperations) {
			OperationStatus status = (OperationStatus) op.getInfo(OperationStatus.KEY);

			if(status != null) switch(status.getStatus()) {
				case NOT_EXECUTED:
				case PRECONDITION_ERROR:
					continue;
				case POSTCONDITION_ERROR:
					ops.add(op);
					ops.add(ResetRepository.singleton);
					break;
				case EXCEPTIONAL:
					ops.add(op);

					// when an exception is thrown, the target is set to null
					if(op instanceof CreateObject) {
						Reference target = ((CreateObject) op).getTarget();
						if(target != null) ops.add(new AssignConstant(target, null));
					} else if(op instanceof Invoke) {
						Reference target = ((Invoke) op).getTarget();
						if(target != null) ops.add(new AssignConstant(target, null));
					} else System.err.println("Unexpected operation: " + op.getClass().getCanonicalName());
					break;
				case SUCCESSFUL:
					manageOperation(ops, op);
			}
			else manageOperation(ops, op);
		}

		System.out.println("Simplifier: from " + test.getTest().length + " -> " + ops.size());

		if(test instanceof TestCoverage) return new TestCoverage(test.getCluster(), test.getReferenceFactory(), ops.toArray(new Operation[ops.size()]), ((TestCoverage) test).getCoverage());
		else return new Test(test.getCluster(), test.getReferenceFactory(), ops.toArray(new Operation[ops.size()]));
	}

	private void manageOperation(List<Operation> ops, Operation op) {
		ops.add(op);

		Reference target = null;
		if(op instanceof CreateObject) target = ((CreateObject) op).getTarget();
		else if(op instanceof Invoke) target = ((Invoke) op).getTarget();

		OperationPrimitiveResult result = (OperationPrimitiveResult) op.getInfo(OperationPrimitiveResult.KEY);
		if(target != null && result != null && result.isSet()) ops.add(new AssignPrimitive(target, result.getValue()));
	}
}
