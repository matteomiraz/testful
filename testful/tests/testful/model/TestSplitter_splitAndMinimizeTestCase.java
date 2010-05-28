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


package testful.model;

import java.util.ArrayList;
import java.util.List;

import testful.GenericTestCase;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.utils.Utils;

/**
 * @author matteo
 *
 */
public class TestSplitter_splitAndMinimizeTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		TrackerDatum[] data = Utils.readData(AnalysisWhiteBox.read(config.getDirInstrumented(), test.getCluster().getCut().getClassName()));

		Test min = TestSplitter.splitAndMinimize(test, GenericTestCase.getFinder(), data);

		List<Test> res = new ArrayList<Test>(1);
		res.add(min);
		return res;
	}
}
