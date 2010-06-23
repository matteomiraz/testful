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

import java.util.Arrays;
import java.util.List;

import testful.GenericTestCase;
import testful.model.AssignPrimitive;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.OperationResult.Status;
import testful.model.Reference;
import testful.model.Test;
import testful.testCut.TestModelArrayStringMatrixCUT;

/**
 * @author matteo
 */
public class SimplifierStaticTestCase extends GenericTestCase {

	protected List<Test> perform(Test test) throws Exception {
		return Arrays.asList(SimplifierStatic.singleton.perform(test));
	}

	/**
	 * java_lang_Integer_0 = (int)0;
	 * test_model_array_StringArray_2 = test_model_array_StringMatrix_3.testful_crea(java_lang_Integer_0);
	 */
	public void testArray() throws Exception {
		TestModelArrayStringMatrixCUT cut = new TestModelArrayStringMatrixCUT();
		Test test = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new AssignPrimitive(cut.ints[0], 0),
				new Invoke(cut.sa[2], cut.cuts[3], cut.c_crea, new Reference[] { cut.ints[0] })
		});

		Operation[][] expected = {
				{
					new AssignPrimitive(cut.ints[0], 0),
					new Invoke(cut.sa[2], null, cut.c_crea, new Reference[] { cut.ints[0] })
				}
		};

		check(test, perform(test), expected, true);
	}

	/**
	 * test_model_array_StringArrayArray_2 = new test.model.array.StringArrayArray();
	 * java_lang_Integer_3 = (int) test_model_array_StringMatrix_1.testful_conta(test_model_array_StringArrayArray_2);
	 * test_model_array_StringArray_1 = test_model_array_StringMatrix_0.testful_crea(java_lang_Integer_3);
	 * test_model_array_StringArrayArray_0 = new test.model.array.StringArrayArray();
	 * test_model_array_StringArray_1 = null;
	 * test_model_array_StringArrayArray_0.addTail(test_model_array_StringArray_1);
	 * java_lang_Integer_2 = (int) test_model_array_StringMatrix_2.testful_conta(test_model_array_StringArrayArray_0);
	 */
	public void testArray1() throws Exception {
		TestModelArrayStringMatrixCUT cut = new TestModelArrayStringMatrixCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.saa[2], cut.saa_cns, new Reference[] { }),
				new Invoke(cut.ints[3], cut.cuts[1], cut.c_conta, new Reference[] { cut.saa[2] }),
				new Invoke(cut.sa[1], cut.cuts[0], cut.c_crea, new Reference[] { cut.ints[3] }),
				new CreateObject(cut.saa[0], cut.saa_cns, new Reference[] { }),
				new AssignPrimitive(cut.sa[1], null),
				new Invoke(null, cut.saa[0], cut.saa_addTail, new Reference[] { cut.sa[1] } ),
				new Invoke(cut.ints[2], cut.cuts[2], cut.c_conta, new Reference[] { cut.saa[0] }),

		});

		t = SimplifierDynamic.singleton.perform(getFinder(), t);

		{
			OperationResult opRes6 = (OperationResult) t.getTest()[6].getInfo(OperationResult.KEY);
			assertNotNull(opRes6);
			assertTrue(opRes6.getStatus() == Status.POSTCONDITION_ERROR);
		}

		t = SimplifierStatic.singleton.perform(t);

		{
			OperationResult opRes6 = (OperationResult) t.getTest()[6].getInfo(OperationResult.KEY);
			assertNotNull(opRes6);
			assertTrue(opRes6.getStatus() == Status.POSTCONDITION_ERROR);
		}
	}

}
