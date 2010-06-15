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

import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.Reference;
import testful.model.Test;
import testful.model.TestExecutionManager;
import testful.testCut.TestCoverageFaultTestCase;

/**
 * @author matteo
 *
 */
public class Splitter_splitObserverFaultTestCase extends TestCoverageFaultTestCase {


	//	  java_lang_Integer_2 = (int) dummy_Simple_3.oStatus();
	//	  java_lang_Object_2 = new dummy.Simple();

	public void test01() throws Exception {
		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[3], oCns, new Reference[] { }),	//1
				new CreateObject(cuts[1], cCns, new Reference[] { }),		//2
				new Invoke(null, cuts[1], c, new Reference[] { }),			//3 -> faulty
				new CreateObject(cuts[1], cCns, new Reference[] { }),		//4
				new Invoke(null, cuts[1], a, new Reference[] { objects[3]})	//5
		});

		OperationResult.insert(test.getTest());
		test = new Test(test.getCluster(), test.getReferenceFactory(), TestExecutionManager.execute(getFinder(), test));

		Test testD = DynamicTransformation.singleton.perform(test);

		List<Test> tests = Splitter.split(true, testD);

		check(test, tests, new Operation[][] {
				{
					new CreateObject(cuts[1], cCns, new Reference[] { }),		//2
					new Invoke(null, cuts[1], c, new Reference[] { }),			//3
				}, {
					new CreateObject(cuts[1], cCns, new Reference[] { }),		//4
					new Invoke(null, cuts[1], a, new Reference[] { objects[3]})	//5
				}
		});
	}
}
