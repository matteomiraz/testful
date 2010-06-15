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

package testful.model.transformation;

import java.util.ArrayList;
import java.util.List;

import testful.AutoTestCase;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.Test;
import testful.model.TestExecutionManager;

/**
 * @author matteo
 */
public class DynamicTransformationAutoTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		List<Test> ret = new ArrayList<Test>(1);

		OperationResult.insert(test.getTest());
		Operation[] ops = TestExecutionManager.execute(getFinder(), test);
		test = new Test(test.getCluster(), test.getReferenceFactory(), ops);
		final Test mod = DynamicTransformation.singleton.perform(test);
		OperationResult.remove(mod);

		ret.add(mod);
		return ret;
	}
}
