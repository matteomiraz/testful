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
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.Test;
import testful.model.TestCoverage;

/**
 * @author matteo
 */
public class TestReducerAutoTestCase extends AutoTestCase {

	@Override
	protected Collection<TestCoverage> perform(Test test) throws Exception {
		TestSuiteReducer reducer = new TestSuiteReducer(getFinder(), true, new TrackerDatum[0]);
		reducer.process(test);
		Collection<TestCoverage> output = reducer.getOutput();

		for (TestCoverage t : output) {
			for (Operation op : t.getTest()) {
				if(op instanceof Invoke || op instanceof CreateObject) {
					if(op.getInfo(OperationResult.KEY) == null) {
						System.err.println(op + " does not have any operation result");
						System.err.println(t);
					}
					assertNotNull(op.getInfo(OperationResult.KEY));
				}
			}
		}

		return output;
	}
}
