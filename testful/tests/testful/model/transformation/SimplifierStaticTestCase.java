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
import testful.model.Invoke;
import testful.model.Operation;
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

}
