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
import testful.model.AssignConstant;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.ResetRepository;
import testful.model.Test;
import testful.model.transformation.ReferenceSorter;
import testful.model.transformation.RemoveUselessDefs;
import testful.model.transformation.Reorganizer;
import testful.model.transformation.SimplifierDynamic;
import testful.model.transformation.SimplifierStatic;
import testful.model.transformation.SingleStaticAssignment;
import testful.model.transformation.TestTransformation;
import testful.model.transformation.TestTransformationPipeline;
import testful.testCut.TestModelArrayStringMatrixCUT;

/**
 * @author matteo
 */
public class JUnitTransformationTestCase extends GenericTestCase {

	private final static TestTransformation transformation = new TestTransformationPipeline(
			SimplifierStatic.singleton,
			SingleStaticAssignment.singleton,
			RemoveUselessDefs.singleton,
			Reorganizer.singleton,
			ReferenceSorter.singleton
	);


	protected List<Test> perform(Test test) throws Exception {
		test = SimplifierDynamic.singleton.perform(getFinder(), test);

		return Arrays.asList(transformation.perform(test));
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
				new AssignConstant(cut.sa[1], null),
				new Invoke(null, cut.saa[0], cut.saa_addTail, new Reference[] { cut.sa[1] } ),
				new Invoke(cut.ints[2], cut.cuts[2], cut.c_conta, new Reference[] { cut.saa[0] }),

		});

		Operation[][] expected = {
				{
					new CreateObject(cut.saa[0], cut.saa_cns, new Reference[] { }),
					new CreateObject(cut.saa[1], cut.saa_cns, new Reference[] { }),
					new Invoke(cut.ints[0], null, cut.c_conta, new Reference[] { cut.saa[0] }),
					new Invoke(null, cut.saa[1], cut.saa_addTail, new Reference[] { cut.sa[0] } ),
					new Invoke(null, null, cut.c_crea, new Reference[] { cut.ints[0] }),
					new Invoke(null, null, cut.c_conta, new Reference[] { cut.saa[1] }),
					ResetRepository.singleton
				}
		};

		check(t, perform(t), expected, true);
	}
}
