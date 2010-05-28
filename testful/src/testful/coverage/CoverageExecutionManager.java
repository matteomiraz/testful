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


package testful.coverage;

import java.util.logging.Logger;

import testful.TestfulException;
import testful.model.Test;
import testful.runner.ClassFinder;
import testful.runner.Context;
import testful.runner.ExecutionManager;
import testful.runner.Executor;
import testful.utils.ElementManager;

public class CoverageExecutionManager extends ExecutionManager<ElementManager<String, CoverageInformation>> {

	private static final Logger logger = Logger.getLogger("testful.coverage");

	public static Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> getContext(ClassFinder finder, Test test, TrackerDatum ... data) {
		Executor executor = new testful.model.executor.ReflectionExecutor(test);
		Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = new Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager>(CoverageExecutionManager.class, finder, executor, data);
		ctx.setStopOnBug(false);
		return ctx;
	}

	public CoverageExecutionManager(byte[] executorSerGz, byte[] trackerDataSerGz, boolean recycleClassLoader) throws TestfulException {
		super(executorSerGz, trackerDataSerGz, recycleClassLoader);
	}

	@Override
	protected ElementManager<String, CoverageInformation> getResult() {
		// if this class has been loaded using the test's classloader, retrieve the result using a simple method call
		if(Tracker.class.getClassLoader() != classLoader) {
			logger.severe("the execution manager must be loaded with the cut's class loader");
			return null;
		}

		ElementManager<String, CoverageInformation> cov = Tracker.getAllCoverage();
		cov.put(new TestSizeInformation(executionTime, executor.getTestLength()));
		return cov;
	}

	@Override
	protected void setup() {
		Tracker.resetAll();
	}

	@Override
	protected void warmUp() {}
}
