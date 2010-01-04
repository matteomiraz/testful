package testful.model;

import testful.TestfulException;
import testful.coverage.TrackerDatum;
import testful.model.executor.ReflectionExecutor;
import testful.runner.ClassFinder;
import testful.runner.Context;
import testful.runner.ExecutionManager;
import testful.runner.Executor;

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
}
