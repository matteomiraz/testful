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

import testful.TestfulException;
import testful.model.Test;
import testful.model.executor.ReflectionExecutor;
import testful.runner.Context;
import testful.runner.DataFinder;
import testful.runner.ExecutionManager;
import testful.utils.ElementManager;

public class CoverageExecutionManager extends ExecutionManager<ElementManager<String, CoverageInformation>> {

	public static Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> getContext(DataFinder finder, Test test, boolean reloadClasses, TrackerDatum ... data) {
		Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = new Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager>(CoverageExecutionManager.class, finder, ReflectionExecutor.class, test, false, data);
		ctx.setReloadClasses(reloadClasses);
		return ctx;
	}

	public CoverageExecutionManager(byte[] executorSer, byte[] trackerDataSer, boolean reloadClasses) throws TestfulException {
		super(executorSer, trackerDataSer, reloadClasses);
	}

	@Override
	protected ElementManager<String, CoverageInformation> getResult() {
		return Tracker.getAllCoverage();
	}

	@Override
	protected void setup() {
		Tracker.resetAll();
	}
}
