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

import testful.GenericTestCase;
import testful.coverage.CoverageInformation;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageBranch;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.model.transformation.Reorganizer;
import testful.testCut.DummySimpleCUT;
import testful.utils.ElementManager;

/**
 * Tests for the TestReorganizer class
 * @author matteo
 */
public class TestReorganizerTestCase extends GenericTestCase {

	public void testDummy01() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test orig = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new Invoke(cut.ints[1], cut.cuts[3], cut.oStatus, new Reference[]{ }),
				new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
				new Invoke(cut.ints[0], cut.cuts[3], cut.oAbs, new Reference[]{ }),
				new CreateObject(cut.objects[3], cut.oCns, new Reference[]{ }),
				new CreateObject(cut.cuts[2], cut.cns, new Reference[]{ }),
				new Invoke(null, cut.cuts[2], cut.compare, new Reference[] { cut.objects[3] }),
				new Invoke(cut.ints[2], cut.cuts[2], cut.oAbs, new Reference[] { })
		});

		Test modified = Reorganizer.singleton.perform(orig);

		ElementManager<String, CoverageInformation> cov = getCoverage(modified);

		assertEquals(9.0f, cov.get(CoverageBasicBlocks.KEY).getQuality());
		assertEquals(4.0f, cov.get(CoverageBranch.KEY).getQuality());

		Operation[] exp = new Operation[]{
				new CreateObject(cut.cuts[2], cut.cns, new Reference[]{ }),
				new CreateObject(cut.objects[3], cut.oCns, new Reference[]{ }),
				new Invoke(cut.ints[1], cut.cuts[3], cut.oStatus, new Reference[]{ }),
				new CreateObject(cut.cuts[3], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[2], cut.compare, new Reference[] { cut.objects[3] }),
				new Invoke(cut.ints[2], cut.cuts[2], cut.oAbs, new Reference[] { }),
				new Invoke(cut.ints[0], cut.cuts[3], cut.oAbs, new Reference[]{ })
		};

		assertEquals(exp.length, modified.getTest().length);
		for (int i = 0; i < exp.length; i++)
			assertEquals(exp[i], modified.getTest()[i]);

	}
}
