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

import java.util.LinkedList;
import java.util.List;

import testful.AutoTestCase;
import testful.model.Test;
import testful.model.transformation.ReferenceSorter;
import testful.model.transformation.RemoveInvalidOperationsStatic;
import testful.model.transformation.RemoveUselessDefs;
import testful.model.transformation.Reorganizer;
import testful.model.transformation.SingleStaticAssignment;
import testful.model.transformation.Splitter;
import testful.model.transformation.TestTransformation;
import testful.model.transformation.TestTransformationPipeline;

/**
 * @author matteo
 *
 */
public class RunnerCachingPrepareAutoTestCase extends AutoTestCase {

	private static final TestTransformation prepare = new TestTransformationPipeline(
			RemoveUselessDefs.singleton,
			RemoveInvalidOperationsStatic.singleton,
			SingleStaticAssignment.singleton
	);

	private static final TestTransformation postprocess = new TestTransformationPipeline(
			RemoveUselessDefs.singleton,
			RemoveInvalidOperationsStatic.singleton,
			SingleStaticAssignment.singleton,
			RemoveUselessDefs.singleton,
			Reorganizer.singleton,
			ReferenceSorter.singleton
	);

	@Override
	protected List<Test> perform(Test test) throws Exception {
		test = prepare.perform(test);

		List<Test> ret = new LinkedList<Test>();
		List<Test> parts = Splitter.split(false, test);
		for(Test p: parts)
			ret.add(postprocess.perform(p));

		return ret;
	}
}
