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

import testful.GenericTestCase;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.testCut.DummySimpleCUT;

public class OptimalTestCase extends GenericTestCase {

	public void testDummySimpleCUT1() throws Exception {
		DummySimpleCUT cut = new DummySimpleCUT();
		Test t1 = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { })
		});
		TestCoverage tc1 = new TestCoverage(t1, getCoverage(t1));

		Test t2 = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
				new Invoke(null, cut.cuts[0], cut.mInc, new Reference[] { })
		});
		TestCoverage tc2 = new TestCoverage(t2, getCoverage(t2));

		assertTrue("Invalid test configuration", tc1.getCoverage().get(CoverageBasicBlocks.KEY).getQuality() < tc2.getCoverage().get(CoverageBasicBlocks.KEY).getQuality());

		OptimalTestCreator opt = new OptimalTestCreator();
		assertEquals(0, opt.get().size());

		opt.update(tc1);

		assertEquals(1, opt.get().size());
		assertEquals(tc1, opt.get().iterator().next());

		opt.update(tc2);
		assertEquals(1, opt.get().size());
		assertEquals(tc2, opt.get().iterator().next());
	}
}
