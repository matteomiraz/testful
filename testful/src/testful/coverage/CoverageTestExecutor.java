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

import testful.model.Test;
import testful.model.executor.TestExecutor;
import testful.model.executor.TestExecutorInput;
import testful.runner.DataFinder;
import testful.runner.Job;
import testful.utils.ElementManager;

public class CoverageTestExecutor extends TestExecutor<ElementManager<String, CoverageInformation>> {

	public static Job<TestExecutorInput, ElementManager<String, CoverageInformation>, CoverageTestExecutor> getContext(DataFinder finder, Test test, boolean reloadClasses, TrackerDatum ... data) {
		Job<TestExecutorInput, ElementManager<String, CoverageInformation>, CoverageTestExecutor> ctx =
			new Job<TestExecutorInput, ElementManager<String, CoverageInformation>, CoverageTestExecutor>(
					CoverageTestExecutor.class, finder, new TestExecutorInput(test, false, data));

		ctx.setReloadClasses(reloadClasses);
		return ctx;
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
