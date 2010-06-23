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

import testful.AutoTestCase;
import testful.model.Test;
import testful.model.transformation.ReferenceSorter;
import testful.model.transformation.RemoveUselessDefs;
import testful.model.transformation.Reorganizer;
import testful.model.transformation.SimplifierDynamic;
import testful.model.transformation.SimplifierStatic;
import testful.model.transformation.SingleStaticAssignment;
import testful.model.transformation.TestTransformation;
import testful.model.transformation.TestTransformationPipeline;

/**
 * @author matteo
 */
public class JUnitTransformationAutoTestCase extends AutoTestCase {

	private final static TestTransformation transformation = new TestTransformationPipeline(
			SimplifierStatic.singleton,
			SingleStaticAssignment.singleton,
			RemoveUselessDefs.singleton,
			Reorganizer.singleton,
			ReferenceSorter.singleton
	);


	@Override
	protected List<Test> perform(Test test) throws Exception {
		test = SimplifierDynamic.singleton.perform(getFinder(), test);

		return Arrays.asList(transformation.perform(test));
	}
}
