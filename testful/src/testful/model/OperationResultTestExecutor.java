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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import testful.coverage.TrackerDatum;
import testful.model.executor.TestExecutor;
import testful.model.executor.TestExecutorInput;
import testful.runner.Context;
import testful.runner.DataFinder;
import testful.runner.RunnerPool;

/**
 * This class executes a test and returns the OperationInformation.
 * @author matteo
 */
public class OperationResultTestExecutor extends TestExecutor<OperationResult[]> {

	@Override
	protected OperationResult[] getResult() {

		Operation[] ops = getInput().getTest().getTest();
		OperationResult[] ret = new OperationResult[ops.length];
		for (int i = 0; i < ops.length; i++)
			ret[i] = (OperationResult) ops[i].getInfo(OperationResult.KEY);

		return ret;
	}

	@Override
	protected void setup() throws ClassNotFoundException {
	}

	public static Future<Test> executeAsync(DataFinder finder, Test origTest, boolean reloadClasses, TrackerDatum ... data) {

		final Test test = origTest.clone();

		Context<TestExecutorInput, OperationResult[], OperationResultTestExecutor> ctx =
			new Context<TestExecutorInput, OperationResult[], OperationResultTestExecutor>(
					OperationResultTestExecutor.class, finder, new TestExecutorInput(test, false, data));

		ctx.setReloadClasses(reloadClasses);

		final Future<OperationResult[]> infosFuture = RunnerPool.getRunnerPool().execute(ctx);
		return new Future<Test>() {

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return infosFuture.cancel(mayInterruptIfRunning);
			}

			@Override
			public boolean isCancelled() {
				return infosFuture.isCancelled();
			}

			@Override
			public boolean isDone() {
				return infosFuture.isDone();
			}

			@Override
			public Test get() throws InterruptedException, ExecutionException {

				OperationResult[] infos = infosFuture.get();

				Operation[] ops = test.getTest();
				for (int i = 0; i < ops.length; i++)
					if(infos[i] != null)
						ops[i].setInfo(infos[i]);

				return test;
			}

			@Override
			public Test get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				OperationResult[] infos = infosFuture.get(timeout, unit);

				Operation[] ops = test.getTest();
				for (int i = 0; i < ops.length; i++)
					if(infos[i] != null)
						ops[i].setInfo(infos[i]);

				return test;
			}

		};

	}

	public static void execute(DataFinder finder, Test test, boolean reloadClasses, TrackerDatum ... data) throws InterruptedException, ExecutionException {

		Context<TestExecutorInput, OperationResult[], OperationResultTestExecutor> ctx =
			new Context<TestExecutorInput, OperationResult[], OperationResultTestExecutor>(
					OperationResultTestExecutor.class, finder, new TestExecutorInput(test, false, data));

		ctx.setReloadClasses(reloadClasses);

		OperationResult[] infos = RunnerPool.getRunnerPool().execute(ctx).get();

		Operation[] ops = test.getTest();
		for (int i = 0; i < ops.length; i++)
			if(infos[i] != null)
				ops[i].setInfo(infos[i]);
	}
}
