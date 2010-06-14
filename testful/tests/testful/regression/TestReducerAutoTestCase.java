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

package testful.regression;

import java.util.Collection;

import testful.AutoTestCase;
import testful.coverage.TrackerDatum;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.regression.TestSuiteReducer;

/**
 * @author matteo
 */
public class TestReducerAutoTestCase extends AutoTestCase {

	@Override
	protected Collection<TestCoverage> perform(Test test) throws Exception {
		TestSuiteReducer reducer = new TestSuiteReducer(getFinder(), new TrackerDatum[0], true);

		reducer.process(test);

		return reducer.getOutput();
	}
}
