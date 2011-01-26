/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010 Matteo Miraz
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

import java.util.Arrays;
import java.util.List;

import testful.GenericTestCase;
import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.Reference;
import testful.model.Test;
import testful.model.TestExecutionManager;
import testful.testCut.DummySimpleCUT;

/**
 * Tests for the TestReorganizer class
 * @author matteo
 */
public class ReorganizerTestCase extends GenericTestCase {

	protected List<Test> perform(Test test) throws Exception {
		OperationResult.insert(test.getTest());
		test = TestExecutionManager.executeTest(getFinder(), new Test(test.getCluster(), test.getReferenceFactory(), test.getTest()), true);

		test = Reorganizer.singleton.perform(test);

		return Arrays.asList(test);
	}

	public void testSimple1() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new Invoke(cut.ints[1], cut.cuts[3], cut.oStatus, new Reference[]{ }),
				new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
				new Invoke(cut.ints[0], cut.cuts[3], cut.oAbs, new Reference[]{ }),
				new CreateObject(cut.objects[3], cut.oCns, new Reference[]{ }),
				new CreateObject(cut.cuts[2], cut.cns, new Reference[]{ }),
				new Invoke(null, cut.cuts[2], cut.compare, new Reference[] { cut.objects[3] }),
				new Invoke(cut.ints[2], cut.cuts[2], cut.oAbs, new Reference[] { })
		});

		Operation[][] expected = {
				{
					new CreateObject(cut.cuts[2], cut.cns, new Reference[]{ }),
					new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
					new CreateObject(cut.objects[3], cut.oCns, new Reference[]{ }),
					new Invoke(null, cut.cuts[2], cut.compare, new Reference[] { cut.objects[3] }),
					new Invoke(cut.ints[0], cut.cuts[3], cut.oAbs, new Reference[]{ }),
					new Invoke(cut.ints[2], cut.cuts[2], cut.oAbs, new Reference[] { })
				}
		};

		check(test, perform(test), expected, true);
	}

	/**
	 * dummy_Simple_1 = null;
	 * java_lang_Integer_0 = (int)-1;
	 * dummy_Simple_3 = new dummy.Simple();
	 * java_lang_Integer_2 = (int) dummy_Simple_3.wModulo(java_lang_Integer_0);
	 * dummy_Simple_1 = new dummy.Simple();
	 * dummy_Simple_1.compare(java_lang_Object_1);
	 */
	public void testSimple2() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignConstant(cut.cuts[1], null),
				new AssignPrimitive(cut.ints[0], -1),
				new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
				new Invoke(cut.ints[2], cut.cuts[3], cut.wModulo, new Reference[] { cut.ints[0] } ),
				new CreateObject(cut.cuts[1], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[1], cut.compare, new Reference[] { cut.objects[1] })
		});


		Operation[][] expected = {
				{
					new AssignConstant(cut.cuts[1], null),
					new AssignPrimitive(cut.ints[0], -1),
					new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
					new Invoke(cut.ints[2], cut.cuts[3], cut.wModulo, new Reference[] { cut.ints[0] } ),
					new CreateObject(cut.cuts[1], cut.cns, new Reference[] { }),
					new Invoke(null, cut.cuts[1], cut.compare, new Reference[] { cut.objects[1] })
				}
		};

		check(test, perform(test), expected, true);
	}

	/**
	 * dummy_Simple_1 = new dummy.Simple();
	 * java_lang_Integer_3 = (int) dummy_Simple_1.oAbs();
	 * dummy_Simple_1.mInc();
	 * dummy_Simple_2 = new dummy.Simple();
	 * java_lang_Integer_0 = (int) dummy_Simple_2.wModulo(java_lang_Integer_3);
	 */
	public void testSimple3() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[1], cut.cns, new Reference[] { }),
				new Invoke(cut.ints[3], cut.cuts[1], cut.oAbs, new Reference[] { }),
				new Invoke(null, cut.cuts[1], cut.mInc, new Reference[] { }),
				new CreateObject(cut.cuts[2], cut.cns, new Reference[] { }),
				new Invoke(cut.ints[0], cut.cuts[2], cut.wModulo, new Reference[] { cut.ints[3] })
		});

		Operation[][] expected = {
				{
					new CreateObject(cut.cuts[1], cut.cns, new Reference[] { }),
					new CreateObject(cut.cuts[2], cut.cns, new Reference[] { }),
					new Invoke(cut.ints[3], cut.cuts[1], cut.oAbs, new Reference[] { }),
					new Invoke(null, cut.cuts[1], cut.mInc, new Reference[] { }),
					new Invoke(cut.ints[0], cut.cuts[2], cut.wModulo, new Reference[] { cut.ints[3] })
				}
		};

		check(t, perform(t), expected, true);

	}
}
