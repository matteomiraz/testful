package testful.model;

import java.util.concurrent.ExecutionException;

import testful.TestfulException;
import testful.coverage.TrackerDatum;
import testful.model.executor.ReflectionExecutor;
import testful.runner.ClassFinder;
import testful.runner.Context;
import testful.runner.ExecutionManager;
import testful.runner.Executor;
import testful.runner.RunnerPool;

public class TestExecutionManager extends ExecutionManager<Operation[]> {

	public static Context<Operation[], TestExecutionManager> getContext(ClassFinder finder, Test test, TrackerDatum ... data) {
		Executor executor = new ReflectionExecutor(test);
		return new Context<Operation[], TestExecutionManager>(TestExecutionManager.class, finder, executor, data);
	}

	public TestExecutionManager(byte[] executorSerGz, byte[] trackerDataSerGz, boolean recycleClassLoader) throws TestfulException {
		super(executorSerGz, trackerDataSerGz, recycleClassLoader);
	}

	@Override
	protected Operation[] getResult() {
		return ((ReflectionExecutor) executor).getTest();
	}

	@Override
	protected void setup() throws ClassNotFoundException {
		Test.ensureNoDuplicateOps(((ReflectionExecutor) executor).getTest());
	}

	@Override
	protected void warmUp() {}


	public static Operation[] getOpStatus(ClassFinder finder, Test test) throws InterruptedException, ExecutionException {
		OperationResult.insert(test.getTest());

		Context<Operation[], TestExecutionManager> ctx = TestExecutionManager.getContext(finder, test);
		ctx.setStopOnBug(false);
		ctx.setRecycleClassLoader(true);

		Operation[] ops = RunnerPool.getRunnerPool().execute(ctx).get();

		for (int i = 0; i < ops.length; i++)
			ops[i] = ops[i].adapt(test.getCluster(), test.getReferenceFactory());

		return ops;
	}
}
