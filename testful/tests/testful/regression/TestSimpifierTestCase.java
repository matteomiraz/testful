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

import java.util.Arrays;
import java.util.List;

import testful.GenericTestCase;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.Reference;
import testful.model.Test;
import testful.model.TestExecutionManager;
import testful.testCut.DummySimpleCUT;

/**
 * @author matteo
 */
public class TestSimpifierTestCase extends GenericTestCase {

	protected List<Test> perform(Test test) throws Exception {
		OperationResult.insert(test.getTest());
		Operation[] ops = TestExecutionManager.execute(getFinder(), test);

		TestSimplifier s = new TestSimplifier();
		Test r = s.process(new Test(test.getCluster(), test.getReferenceFactory(), ops));

		return Arrays.asList(r);
	}

	public void testSimpleDummy1() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.mInc, new Reference[] { })
		});

		Operation[][] expected = {
				{
					new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
					new Invoke(null, cut.cuts[0], cut.mInc, new Reference[] { })
				}
		};

		check(test, perform(test), expected);
	}
}
