/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.model;

import java.util.concurrent.ExecutionException;

import testful.TestfulException;
import testful.coverage.TrackerDatum;
import testful.model.executor.ReflectionExecutor;
import testful.runner.Context;
import testful.runner.DataFinder;
import testful.runner.ExecutionManager;
import testful.runner.RunnerPool;

/**
 * This class executes a test and returns the OperationInformation.
 * @author matteo
 */
public class TestExecutionManager extends ExecutionManager<Operation[]> {

	public TestExecutionManager(byte[] executorSer, byte[] trackerDataSer, boolean reloadClasses) throws TestfulException {
		super(executorSer, trackerDataSer, reloadClasses);
	}

	@Override
	protected Operation[] getResult() {
		return executor.getTest();
	}

	@Override
	protected void setup() throws ClassNotFoundException {
		Test.ensureNoDuplicateOps(executor.getTest());
	}

	public static Operation[] execute(DataFinder finder, Test test, boolean reloadClasses, TrackerDatum ... data) throws InterruptedException, ExecutionException {
		Context<Operation[], TestExecutionManager> ctx =  new Context<Operation[], TestExecutionManager>(TestExecutionManager.class, finder, ReflectionExecutor.class, test, false, data);
		ctx.setReloadClasses(reloadClasses);

		Operation[] ops = RunnerPool.getRunnerPool().execute(ctx).get();

		for (int i = 0; i < ops.length; i++)
			ops[i] = ops[i].adapt(test.getCluster(), test.getReferenceFactory());

		return ops;
	}

	public static Test executeTest(DataFinder finder, Test test, boolean reloadClasses, TrackerDatum ... data) throws InterruptedException, ExecutionException {
		Context<Operation[], TestExecutionManager> ctx =  new Context<Operation[], TestExecutionManager>(TestExecutionManager.class, finder, ReflectionExecutor.class, test, false, data);
		ctx.setReloadClasses(reloadClasses);

		Operation[] ops = RunnerPool.getRunnerPool().execute(ctx).get();

		for (int i = 0; i < ops.length; i++)
			ops[i] = ops[i].adapt(test.getCluster(), test.getReferenceFactory());

		return new Test(test.getCluster(), test.getReferenceFactory(), ops);
	}

}
