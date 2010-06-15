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

import java.util.List;

import testful.GenericTestCase;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.Reference;
import testful.model.Test;
import testful.model.TestExecutionManager;
import testful.testCut.TestCoverageFaultCUT;

/**
 * @author matteo
 *
 */
public class Splitter_splitObserverFaultTestCase extends GenericTestCase {

	public void testFault() throws Exception {
		TestCoverageFaultCUT cut = new TestCoverageFaultCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.objects[3], cut.oCns, new Reference[] { }),		//1
				new CreateObject(cut.cuts[1], cut.cCns, new Reference[] { }),			//2
				new Invoke(null, cut.cuts[1], cut.c, new Reference[] { }),				//3 -> faulty
				new CreateObject(cut.cuts[1], cut.cCns, new Reference[] { }),			//4
				new Invoke(null, cut.cuts[1], cut.a, new Reference[] { cut.objects[3]})	//5
		});

		OperationResult.insert(test.getTest());
		test = new Test(test.getCluster(), test.getReferenceFactory(), TestExecutionManager.execute(getFinder(), test));

		Test testD = DynamicTransformation.singleton.perform(test);

		List<Test> tests = Splitter.split(true, testD);

		check(test, tests, new Operation[][] {
				{
					new CreateObject(cut.cuts[1], cut.cCns, new Reference[] { }),			//2
					new Invoke(null, cut.cuts[1], cut.c, new Reference[] { }),				//3
				}, {
					new CreateObject(cut.cuts[1], cut.cCns, new Reference[] { }),			//4
					new Invoke(null, cut.cuts[1], cut.a, new Reference[] { cut.objects[3]})	//5
				}
		});
	}
}
