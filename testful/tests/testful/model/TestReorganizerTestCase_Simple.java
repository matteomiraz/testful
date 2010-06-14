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

package testful.model;

import testful.coverage.CoverageInformation;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageBranch;
import testful.model.transformation.Reorganizer;
import testful.testCut.DummySimpleTestCase;
import testful.utils.ElementManager;

/**
 * Tests for the TestReorganizer class
 * @author matteo
 */
public class TestReorganizerTestCase_Simple extends DummySimpleTestCase {

	public void test01() throws Exception {

		Test orig = new Test(cluster, refFactory, new Operation[] {
				new Invoke(ints[1], cuts[3], oStatus, new Reference[]{ }),
				new CreateObject(cuts[3], cns, new Reference[] { }),
				new Invoke(ints[0], cuts[3], oAbs, new Reference[]{ }),
				new CreateObject(objects[3], oCns, new Reference[]{ }),
				new CreateObject(cuts[2], cns, new Reference[]{ }),
				new Invoke(null, cuts[2], compare, new Reference[] { objects[3] }),
				new Invoke(ints[2], cuts[2], oAbs, new Reference[] { })
		});

		Test modified = Reorganizer.singleton.perform(orig);

		ElementManager<String, CoverageInformation> cov = getCoverage(modified);

		assertEquals(9.0f, cov.get(CoverageBasicBlocks.KEY).getQuality());
		assertEquals(4.0f, cov.get(CoverageBranch.KEY).getQuality());

		Operation[] exp = new Operation[]{
				new CreateObject(cuts[2], cns, new Reference[]{ }),
				new CreateObject(objects[3], oCns, new Reference[]{ }),
				new Invoke(ints[1], cuts[3], oStatus, new Reference[]{ }),
				new CreateObject(cuts[3], cns, new Reference[] { }),
				new Invoke(null, cuts[2], compare, new Reference[] { objects[3] }),
				new Invoke(ints[2], cuts[2], oAbs, new Reference[] { }),
				new Invoke(ints[0], cuts[3], oAbs, new Reference[]{ })
		};

		assertEquals(exp.length, modified.getTest().length);
		for (int i = 0; i < exp.length; i++)
			assertEquals(exp[i], modified.getTest()[i]);

	}
}
